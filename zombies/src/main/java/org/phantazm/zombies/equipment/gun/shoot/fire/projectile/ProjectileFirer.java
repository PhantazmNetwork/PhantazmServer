package org.phantazm.zombies.equipment.gun.shoot.fire.projectile;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.MobSpawner;
import org.phantazm.mob2.goal.CollectionGoalGroup;
import org.phantazm.mob2.goal.ProjectileMovementGoal;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.event.GunShootEvent;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;
import org.phantazm.zombies.equipment.gun.shoot.endpoint.ShotEndpointSelector;
import org.phantazm.zombies.equipment.gun.shoot.fire.Firer;
import org.phantazm.zombies.equipment.gun.shoot.handler.ShotHandler;
import org.phantazm.zombies.equipment.gun.target.TargetFinder;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Supplier;

/**
 * A {@link Firer} that works by firing projectiles.
 */
@Model("zombies.gun.firer.projectile")
@Cache(false)
public class ProjectileFirer implements Firer {
    private final PriorityQueue<AliveProjectile> removalQueue =
        new PriorityQueue<>(Comparator.comparingLong(AliveProjectile::ticks));
    private final Map<UUID, FiredShot> firedShots = new HashMap<>();
    private final Data data;
    private final Supplier<Optional<? extends Entity>> entitySupplier;
    private final UUID shooterUUID;
    private final ShotEndpointSelector endSelector;
    private final TargetFinder targetFinder;
    private final ProjectileCollisionFilter collisionFilter;
    private final Collection<ShotHandler> shotHandlers;
    private final MobSpawner spawner;
    private long ticks = 0;

    /**
     * Creates a new {@link ProjectileFirer}.
     *
     * @param data            The {@link Data} of the {@link ProjectileFirer}
     * @param entitySupplier  A {@link Supplier} of the {@link Entity} shooter
     * @param shooterUUID     The UUID of the {@link Entity} shooter
     * @param endSelector     The {@link ShotEndpointSelector} of the {@link ProjectileFirer}
     * @param targetFinder    The {@link TargetFinder} of the {@link ProjectileFirer}
     * @param collisionFilter The {@link ProjectileCollisionFilter} of the {@link ProjectileFirer}
     * @param shotHandlers    The {@link ShotHandler}s of the {@link ProjectileFirer}
     */
    @FactoryMethod
    public ProjectileFirer(@NotNull Data data, @NotNull Supplier<Optional<? extends Entity>> entitySupplier,
        @NotNull UUID shooterUUID,
        @NotNull @Child("end_selector") ShotEndpointSelector endSelector,
        @NotNull @Child("target_finder") TargetFinder targetFinder,
        @NotNull @Child("collision_filter") ProjectileCollisionFilter collisionFilter,
        @NotNull @Child("shot_handlers") Collection<ShotHandler> shotHandlers, @NotNull MobSpawner spawner) {
        this.data = Objects.requireNonNull(data);
        this.entitySupplier = Objects.requireNonNull(entitySupplier);
        this.shooterUUID = Objects.requireNonNull(shooterUUID);
        this.endSelector = Objects.requireNonNull(endSelector);
        this.targetFinder = Objects.requireNonNull(targetFinder);
        this.collisionFilter = Objects.requireNonNull(collisionFilter);
        this.shotHandlers = List.copyOf(shotHandlers);
        this.spawner = Objects.requireNonNull(spawner);
    }

    @Override
    public void fire(@NotNull Gun gun, @NotNull GunState state, @NotNull Pos start,
        @NotNull Collection<UUID> previousHits) {
        entitySupplier.get().ifPresent(shooter -> {
            Instance instance = shooter.getInstance();
            if (instance == null) {
                return;
            }

            endSelector.getEnd(start).ifPresent(end -> {
                if (!spawner.canSpawn(data.projectileMob)) {
                    return;
                }

                Mob mob = spawner.spawn(data.projectileMob, instance, start, self -> {
                    self.addGoalGroup(new CollectionGoalGroup(Set.of(new ProjectileMovementGoal(self, shooter, end,
                        data.power(), data.spread(), this::onProjectileCollision, this::onProjectileCollision))));
                    self.setNoGravity(!data.hasGravity());
                });

                firedShots.put(mob.getUuid(), new FiredShot(gun, state, shooter, start, previousHits));
                removalQueue.add(new AliveProjectile(new WeakReference<>(mob), ticks));
            });
        });
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        ++ticks;
        for (AliveProjectile aliveProjectile = removalQueue.peek();
             aliveProjectile != null && ticks - aliveProjectile.ticks() > data.maxAliveTime();
             aliveProjectile = removalQueue.peek()) {
            Entity projectile = aliveProjectile.projectile().get();
            if (projectile == null) {
                return;
            }

            FiredShot firedShot = firedShots.get(projectile.getUuid());
            if (firedShot == null) {
                projectile.remove();
                return;
            }

            onProjectileCollision(firedShot, projectile, projectile.getPosition());
        }

        for (ShotHandler shotHandler : shotHandlers) {
            shotHandler.tick(state, time);
        }
    }

    /**
     * Called when a projectile comes into contact with a block.
     *
     * @param event The associated {@link ProjectileCollideWithBlockEvent}
     */
    public void onProjectileCollision(@NotNull ProjectileCollideWithBlockEvent event) {
        FiredShot firedShot = firedShots.get(event.getEntity().getUuid());
        if (firedShot == null) {
            return;
        }

        onProjectileCollision(firedShot, event.getEntity(), event.getCollisionPosition());
    }

    /**
     * Called when a projectile comes into contact with an entity.
     *
     * @param event The associated {@link ProjectileCollideWithEntityEvent}
     */
    public void onProjectileCollision(@NotNull ProjectileCollideWithEntityEvent event) {
        if (!(collisionFilter.shouldExplode(event.getTarget()))) {
            return;
        }

        FiredShot firedShot = firedShots.get(event.getEntity().getUuid());
        if (firedShot == null) {
            return;
        }

        onProjectileCollision(firedShot, event.getEntity(), event.getCollisionPosition());
    }

    private void onProjectileCollision(@NotNull FiredShot firedShot, @NotNull Entity projectile,
        @NotNull Point collision) {
        if (firedShot.shooter().getUuid().equals(shooterUUID)) {
            TargetFinder.Result target = targetFinder.findTarget(firedShot.gun, firedShot.shooter(), firedShot.start(),
                collision, firedShot.previousHits());

            target.regular().removeIf(hit -> hit.entity().getUuid().equals(projectile.getUuid()));
            target.headshot().removeIf(hit -> hit.entity().getUuid().equals(projectile.getUuid()));

            for (GunHit hit : target.regular()) {
                firedShot.previousHits().add(hit.entity().getUuid());
            }
            for (GunHit hit : target.headshot()) {
                firedShot.previousHits().add(hit.entity().getUuid());
            }

            GunShot shot = new GunShot(firedShot.start(), collision, target.regular(), target.headshot());
            EventDispatcher.call(new GunShootEvent(firedShot.gun, shot, firedShot.shooter()));
            for (ShotHandler shotHandler : shotHandlers) {
                shotHandler.handle(firedShot.gun(), firedShot.state(), firedShot.shooter(), firedShot.previousHits(),
                    shot);
            }
        }

        projectile.remove();
        removalQueue.removeIf(aliveProjectile -> aliveProjectile.projectile().refersTo(projectile));
    }

    /**
     * Data for a {@link ProjectileFirer}.
     *
     * @param endSelector     A path to the {@link ProjectileFirer}'s {@link ShotEndpointSelector}
     * @param targetFinder    A path to the {@link ProjectileFirer}'s {@link TargetFinder}
     * @param collisionFilter A path to the {@link ProjectileFirer}'s {@link ProjectileCollisionFilter}
     * @param shotHandlers    A {@link Collection} of paths to the {@link ProjectileFirer}'s {@link ShotHandler}s
     * @param power           The power of the {@link ProjectileFirer}'s projectiles
     * @param spread          The spread of the {@link ProjectileFirer}'s projectiles
     * @param hasGravity      Whether the {@link ProjectileFirer}'s projectiles have gravity
     * @param maxAliveTime    The maximum time, in ticks, that the {@link ProjectileFirer}'s projectiles can live before
     *                        automatically exploding
     */
    @DataObject
    public record Data(
        @NotNull @ChildPath("end_selector") String endSelector,
        @NotNull @ChildPath("target_finder") String targetFinder,
        @NotNull @ChildPath("collision_filter") String collisionFilter,
        @NotNull @ChildPath("shot_handlers") Collection<String> shotHandlers,
        @NotNull Key projectileMob,
        double power,
        double spread,
        boolean hasGravity,
        long maxAliveTime) {
    }

    private record FiredShot(
        @NotNull Gun gun,
        @NotNull GunState state,
        @NotNull Entity shooter,
        @NotNull Pos start,
        @NotNull Collection<UUID> previousHits) {

        private FiredShot {
            Objects.requireNonNull(state);
            Objects.requireNonNull(shooter);
            Objects.requireNonNull(start);
            Objects.requireNonNull(previousHits);
        }

    }

    private record AliveProjectile(@NotNull Reference<Entity> projectile,
        long ticks) {

        private AliveProjectile {
            Objects.requireNonNull(projectile);
        }

    }

}
