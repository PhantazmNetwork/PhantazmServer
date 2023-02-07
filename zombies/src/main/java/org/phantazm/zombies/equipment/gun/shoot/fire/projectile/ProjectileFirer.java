package org.phantazm.zombies.equipment.gun.shoot.fire.projectile;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobModel;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.goal.CollectionGoalGroup;
import org.phantazm.mob.goal.ProjectileMovementGoal;
import org.phantazm.mob.spawner.MobSpawner;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
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
            new PriorityQueue<>(Comparator.comparingLong(AliveProjectile::time));
    private final Map<UUID, FiredShot> firedShots = new HashMap<>();
    private final Data data;
    private final Supplier<Optional<? extends Entity>> entitySupplier;
    private final UUID shooterUUID;
    private final ShotEndpointSelector endSelector;
    private final TargetFinder targetFinder;
    private final ProjectileCollisionFilter collisionFilter;
    private final Collection<ShotHandler> shotHandlers;

    private final MobStore mobStore;

    private final MobSpawner spawner;

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
            @NotNull UUID shooterUUID, @NotNull @Child("end_selector") ShotEndpointSelector endSelector,
            @NotNull @Child("target_finder") TargetFinder targetFinder,
            @NotNull @Child("collision_filter") ProjectileCollisionFilter collisionFilter,
            @NotNull @Child("shot_handlers") Collection<ShotHandler> shotHandlers, @NotNull MobStore mobStore,
            @NotNull MobSpawner spawner, @NotNull EventNode<Event> node) {
        this.data = Objects.requireNonNull(data, "data");
        this.entitySupplier = Objects.requireNonNull(entitySupplier, "entitySupplier");
        this.shooterUUID = Objects.requireNonNull(shooterUUID, "shooterUUID");
        this.endSelector = Objects.requireNonNull(endSelector, "endSelector");
        this.targetFinder = Objects.requireNonNull(targetFinder, "targetFinder");
        this.collisionFilter = Objects.requireNonNull(collisionFilter, "collisionFilter");
        this.shotHandlers = List.copyOf(shotHandlers);
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.spawner = Objects.requireNonNull(spawner, "spawner");

        node.addListener(ProjectileCollideWithBlockEvent.class, this::onProjectileCollision);
        node.addListener(ProjectileCollideWithEntityEvent.class, this::onProjectileCollision);
    }

    @Override
    public void fire(@NotNull Gun gun, @NotNull GunState state, @NotNull Pos start,
            @NotNull Collection<UUID> previousHits) {
        entitySupplier.get().ifPresent(entity -> {
            Instance instance = entity.getInstance();
            if (instance == null) {
                return;
            }

            endSelector.getEnd(start).ifPresent(end -> {
                PhantazmMob mob = spawner.spawn(instance, start, mobStore, data.model());
                ProximaEntity neuralEntity = mob.entity();
                neuralEntity.addGoalGroup(new CollectionGoalGroup(Collections.singleton(
                        new ProjectileMovementGoal(neuralEntity, entity, end, data.power(), data.spread()))));
                neuralEntity.setNoGravity(!data.hasGravity());
                mobStore.registerMob(mob);

                firedShots.put(neuralEntity.getUuid(), new FiredShot(gun, state, entity, start, previousHits));
                removalQueue.add(new AliveProjectile(new WeakReference<>(neuralEntity), System.currentTimeMillis()));
            });
        });
    }

    @Override
    public void addExtraShotHandler(ShotHandler shotHandler) {

    }

    @Override
    public void removeExtraShotHandler(ShotHandler shotHandler) {

    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        for (AliveProjectile aliveProjectile = removalQueue.peek();
                aliveProjectile != null && (time - aliveProjectile.time()) / 50 > data.maxAliveTime();
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
        if (!(event.getEntity() instanceof EntityProjectile projectile)) {
            return;
        }

        FiredShot firedShot = firedShots.get(projectile.getUuid());
        if (firedShot == null) {
            return;
        }

        onProjectileCollision(firedShot, projectile, event.getCollisionPosition());
    }

    /**
     * Called when a projectile comes into contact with an entity.
     *
     * @param event The associated {@link ProjectileCollideWithEntityEvent}
     */
    public void onProjectileCollision(@NotNull ProjectileCollideWithEntityEvent event) {
        if (!(event.getEntity() instanceof EntityProjectile projectile &&
                collisionFilter.shouldExplode(event.getTarget()))) {
            return;
        }

        FiredShot firedShot = firedShots.get(projectile.getUuid());
        if (firedShot == null) {
            return;
        }

        onProjectileCollision(firedShot, projectile, event.getCollisionPosition());
    }

    private void onProjectileCollision(@NotNull FiredShot firedShot, @NotNull Entity projectile,
            @NotNull Point collision) {
        if (firedShot.shooter().getUuid().equals(shooterUUID)) {
            TargetFinder.Result target = targetFinder.findTarget(firedShot.shooter(), firedShot.start(), collision,
                    firedShot.previousHits());
            for (GunHit hit : target.regular()) {
                firedShot.previousHits().add(hit.entity().getUuid());
            }
            for (GunHit hit : target.headshot()) {
                firedShot.previousHits().add(hit.entity().getUuid());
            }
            for (ShotHandler shotHandler : shotHandlers) {
                GunShot shot = new GunShot(firedShot.start(), collision, target.regular(), target.headshot());
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
     * @param endSelectorPath     A path to the {@link ProjectileFirer}'s {@link ShotEndpointSelector}
     * @param targetFinderPath    A path to the {@link ProjectileFirer}'s {@link TargetFinder}
     * @param collisionFilterPath A path to the {@link ProjectileFirer}'s {@link ProjectileCollisionFilter}
     * @param shotHandlerPaths    A {@link Collection} of paths to the {@link ProjectileFirer}'s {@link ShotHandler}s
     * @param power               The power of the {@link ProjectileFirer}'s projectiles
     * @param spread              The spread of the {@link ProjectileFirer}'s projectiles
     * @param hasGravity          Whether the {@link ProjectileFirer}'s projectiles have gravity
     * @param maxAliveTime        The maximum time, in ticks, that the {@link ProjectileFirer}'s projectiles can live
     *                            before automatically exploding
     */
    @DataObject
    public record Data(@NotNull @ChildPath("end_selector") String endSelectorPath,
                       @NotNull @ChildPath("target_finder") String targetFinderPath,
                       @NotNull @ChildPath("collision_filter") String collisionFilterPath,
                       @NotNull @ChildPath("shot_handlers") Collection<String> shotHandlerPaths,
                       @NotNull MobModel model,
                       double power,
                       double spread,
                       boolean hasGravity,
                       long maxAliveTime) {

        /**
         * Creates a {@link Data}.
         *
         * @param endSelectorPath     A path to the {@link ProjectileFirer}'s {@link ShotEndpointSelector}
         * @param targetFinderPath    A path to the {@link ProjectileFirer}'s {@link TargetFinder}
         * @param collisionFilterPath A path to the {@link ProjectileFirer}'s {@link ProjectileCollisionFilter}
         * @param shotHandlerPaths    A {@link Collection} of paths to the {@link ProjectileFirer}'s {@link ShotHandler}s
         * @param power               The power of the {@link ProjectileFirer}'s projectiles
         * @param spread              The spread of the {@link ProjectileFirer}'s projectiles
         * @param hasGravity          Whether the {@link ProjectileFirer}'s projectiles have gravity
         * @param maxAliveTime        The maximum time, in ticks, that the {@link ProjectileFirer}'s projectiles can live
         */
        public Data {
            Objects.requireNonNull(endSelectorPath, "endSelectorPath");
            Objects.requireNonNull(targetFinderPath, "targetFinderPath");
            Objects.requireNonNull(collisionFilterPath, "collisionFilterPath");
            Objects.requireNonNull(shotHandlerPaths, "shotHandlerPaths");
            Objects.requireNonNull(model, "model");
        }

    }

    private record FiredShot(@NotNull Gun gun,
                             @NotNull GunState state,
                             @NotNull Entity shooter,
                             @NotNull Pos start,
                             @NotNull Collection<UUID> previousHits) {

        private FiredShot {
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(shooter, "shooter");
            Objects.requireNonNull(start, "start");
            Objects.requireNonNull(previousHits, "previousHits");
        }

    }

    private record AliveProjectile(@NotNull Reference<Entity> projectile, long time) {

        private AliveProjectile {
            Objects.requireNonNull(projectile, "projectile");
        }

    }

}
