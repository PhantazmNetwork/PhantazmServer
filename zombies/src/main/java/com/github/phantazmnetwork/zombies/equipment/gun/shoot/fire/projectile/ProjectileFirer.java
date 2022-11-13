package com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.projectile;

import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.goal.ProjectileMovementGoal;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.NeuralEntity;
import com.github.phantazmnetwork.neuron.bindings.minestom.entity.goal.GoalGroup;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint.ShotEndpointSelector;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.Firer;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler.ShotHandler;
import com.github.phantazmnetwork.zombies.equipment.gun.target.TargetFinder;
import com.github.steanky.element.core.ElementFactory;
import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
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

    private static final ElementFactory<Data, ProjectileFirer> FACTORY = (objectData, context, dependencyProvider) -> {
        EventNode<Event> node = dependencyProvider.provide(Key.key("zombies.dependency.gun.event_node"));

        Supplier<Optional<? extends Entity>> entitySupplier =
                dependencyProvider.provide(Key.key("zombies.dependency.gun.shooter.supplier"));
        UUID shooterUUID = dependencyProvider.provide(Key.key("zombies.dependency.gun.firer.projectile.shooter.uuid"));
        ShotEndpointSelector endpointSelector =
                context.provide(objectData.endSelectorPath(), dependencyProvider, false);
        TargetFinder targetFinder = context.provide(objectData.targetFinderPath(), dependencyProvider, false);
        ProjectileCollisionFilter collisionFilter =
                context.provide(objectData.collisionFilterPath(), dependencyProvider, false);
        Collection<ShotHandler> shotHandlers = new ArrayList<>(objectData.shotHandlerPaths().size());
        for (String shotHandlerPath : objectData.shotHandlerPaths()) {
            shotHandlers.add(context.provide(shotHandlerPath, dependencyProvider, false));
        }
        MobStore mobStore = dependencyProvider.provide(Key.key("zombies.dependency.gun.store"));
        MobSpawner spawner = dependencyProvider.provide(Key.key("zombies.dependency.gun.spawner"));

        ProjectileFirer firer =
                new ProjectileFirer(objectData, entitySupplier, shooterUUID, endpointSelector, targetFinder,
                        collisionFilter, shotHandlers, mobStore, spawner);
        node.addListener(ProjectileCollideWithBlockEvent.class, firer::onProjectileCollision);
        node.addListener(ProjectileCollideWithEntityEvent.class, firer::onProjectileCollision);

        return firer;
    };

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
    public ProjectileFirer(@NotNull Data data, @NotNull Supplier<Optional<? extends Entity>> entitySupplier,
            @NotNull UUID shooterUUID, @NotNull ShotEndpointSelector endSelector, @NotNull TargetFinder targetFinder,
            @NotNull ProjectileCollisionFilter collisionFilter, @NotNull Collection<ShotHandler> shotHandlers,
            @NotNull MobStore mobStore, @NotNull MobSpawner spawner) {
        this.data = Objects.requireNonNull(data, "data");
        this.entitySupplier = Objects.requireNonNull(entitySupplier, "entitySupplier");
        this.shooterUUID = Objects.requireNonNull(shooterUUID, "shooterUUID");
        this.endSelector = Objects.requireNonNull(endSelector, "endSelector");
        this.targetFinder = Objects.requireNonNull(targetFinder, "targetFinder");
        this.collisionFilter = Objects.requireNonNull(collisionFilter, "collisionFilter");
        this.shotHandlers = List.copyOf(shotHandlers);
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.spawner = Objects.requireNonNull(spawner, "spawner");
    }

    @FactoryMethod
    public static @NotNull ElementFactory<Data, ProjectileFirer> factory() {
        return FACTORY;
    }

    @Override
    public void fire(@NotNull GunState state, @NotNull Pos start, @NotNull Collection<UUID> previousHits) {
        entitySupplier.get().ifPresent(entity -> {
            Instance instance = entity.getInstance();
            if (instance == null) {
                return;
            }

            endSelector.getEnd(start).ifPresent(end -> {
                PhantazmMob mob = spawner.spawn(instance, start, mobStore, data.model());
                NeuralEntity neuralEntity = mob.entity();
                neuralEntity.addGoalGroup(new GoalGroup(Collections.singleton(
                        new ProjectileMovementGoal(neuralEntity, entity, end, data.power(), data.spread()))));
                neuralEntity.setNoGravity(!data.hasGravity());
                mobStore.registerMob(mob);

                firedShots.put(neuralEntity.getUuid(), new FiredShot(state, entity, start, previousHits));
                removalQueue.add(new AliveProjectile(new WeakReference<>(neuralEntity), System.currentTimeMillis()));
            });
        });
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
                shotHandler.handle(firedShot.state(), firedShot.shooter(), firedShot.previousHits(), shot);
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
    public record Data(@NotNull String endSelectorPath,
                       @NotNull String targetFinderPath,
                       @NotNull String collisionFilterPath,
                       @NotNull Collection<String> shotHandlerPaths,
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

    private record FiredShot(@NotNull GunState state,
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
