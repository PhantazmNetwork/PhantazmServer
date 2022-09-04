package com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.projectile;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.MobModel;
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
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * A {@link Firer} that works by firing projectiles.
 */
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
            @NotNull MobSpawner spawner) {
        this.data = Objects.requireNonNull(data, "data");
        this.entitySupplier = Objects.requireNonNull(entitySupplier, "entitySupplier");
        this.shooterUUID = Objects.requireNonNull(shooterUUID, "shooterUUID");
        this.endSelector = Objects.requireNonNull(endSelector, "endSelector");
        this.targetFinder = Objects.requireNonNull(targetFinder, "targetFinder");
        this.collisionFilter = Objects.requireNonNull(collisionFilter, "collisionFilter");
        this.shotHandlers = List.copyOf(shotHandlers);
        this.spawner = Objects.requireNonNull(spawner, "spawner");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor(@NotNull ConfigProcessor<MobModel> modelProcessor) {
        ConfigProcessor<Key> keyProcessor = ConfigProcessors.key();
        ConfigProcessor<Collection<Key>> collectionProcessor = keyProcessor.collectionProcessor();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key endSelectorKey = keyProcessor.dataFromElement(element.getElementOrThrow("endSelector"));
                Key targetFinderKey = keyProcessor.dataFromElement(element.getElementOrThrow("targetFinder"));
                Key collisionFilterKey = keyProcessor.dataFromElement(element.getElementOrThrow("collisionFilter"));
                Collection<Key> shotHandlerKeys =
                        collectionProcessor.dataFromElement(element.getElementOrThrow("shotHandlers"));
                MobModel model = modelProcessor.dataFromElement(element.getElementOrThrow("model"));
                double power = element.getNumberOrThrow("power").doubleValue();
                double spread = element.getNumberOrThrow("spread").doubleValue();
                boolean hasGravity = element.getBooleanOrThrow("hasGravity");
                long maxAliveTime = element.getNumberOrThrow("maxAliveTime").longValue();
                if (maxAliveTime < 0) {
                    throw new ConfigProcessException("maxAliveTime must be greater than or equal to 0");
                }

                return new Data(endSelectorKey, targetFinderKey, collisionFilterKey, shotHandlerKeys, model, power,
                        spread, hasGravity, maxAliveTime);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(9);
                node.put("endSelector", keyProcessor.elementFromData(data.endSelectorKey()));
                node.put("targetFinder", keyProcessor.elementFromData(data.targetFinderKey()));
                node.put("shotHandlers", collectionProcessor.elementFromData(data.shotHandlerKeys()));
                node.put("collisionFilter", keyProcessor.elementFromData(data.collisionFilterKey()));
                node.put("model", modelProcessor.elementFromData(data.model()));
                node.putNumber("power", data.power());
                node.putNumber("spread", data.spread());
                node.putBoolean("hasGravity", data.hasGravity());
                node.putNumber("maxAliveTime", data.maxAliveTime());

                return node;
            }
        };
    }

    /**
     * Creates a dependency consumer for {@link Data}s.
     *
     * @return A dependency consumer for {@link Data}s
     */
    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> {
            keys.add(data.endSelectorKey());
            keys.add(data.targetFinderKey());
            keys.add(data.collisionFilterKey());
            keys.addAll(data.shotHandlerKeys());
        };
    }

    @Override
    public void fire(@NotNull GunState state, @NotNull Pos start, @NotNull Collection<UUID> previousHits) {
        entitySupplier.get().ifPresent(entity -> {
            Instance instance = entity.getInstance();
            if (instance == null) {
                return;
            }

            endSelector.getEnd(start).ifPresent(end -> {
                NeuralEntity neuralEntity = spawner.spawn(instance, start, data.model()).entity();
                neuralEntity.addGoalGroup(new GoalGroup(Collections.singleton(
                        new ProjectileMovementGoal(neuralEntity, entity, end, data.power(), data.spread()))));
                neuralEntity.setNoGravity(!data.hasGravity());

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
     * @param endSelectorKey     A {@link Key} to the {@link ProjectileFirer}'s {@link ShotEndpointSelector}
     * @param targetFinderKey    A {@link Key} to the {@link ProjectileFirer}'s {@link TargetFinder}
     * @param collisionFilterKey A {@link Key} to the {@link ProjectileFirer}'s {@link ProjectileCollisionFilter}
     * @param shotHandlerKeys    A {@link Collection} of {@link Key}s to the {@link ProjectileFirer}'s {@link ShotHandler}s
     * @param power              The power of the {@link ProjectileFirer}'s projectiles
     * @param spread             The spread of the {@link ProjectileFirer}'s projectiles
     * @param hasGravity         Whether the {@link ProjectileFirer}'s projectiles have gravity
     * @param maxAliveTime       The maximum time, in ticks, that the {@link ProjectileFirer}'s projectiles can live
     *                           before automatically exploding
     */
    public record Data(@NotNull Key endSelectorKey,
                       @NotNull Key targetFinderKey,
                       @NotNull Key collisionFilterKey,
                       @NotNull Collection<Key> shotHandlerKeys,
                       @NotNull MobModel model,
                       double power,
                       double spread,
                       boolean hasGravity,
                       long maxAliveTime) implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.firer.projectile");

        /**
         * Creates a {@link Data}.
         *
         * @param endSelectorKey     A {@link Key} to the {@link ProjectileFirer}'s {@link ShotEndpointSelector}
         * @param targetFinderKey    A {@link Key} to the {@link ProjectileFirer}'s {@link TargetFinder}
         * @param collisionFilterKey A {@link Key} to the {@link ProjectileFirer}'s {@link ProjectileCollisionFilter}
         * @param shotHandlerKeys    A {@link Collection} of {@link Key}s to the {@link ProjectileFirer}'s {@link ShotHandler}s
         * @param power              The power of the {@link ProjectileFirer}'s projectiles
         * @param spread             The spread of the {@link ProjectileFirer}'s projectiles
         * @param hasGravity         Whether the {@link ProjectileFirer}'s projectiles have gravity
         * @param maxAliveTime       The maximum time, in ticks, that the {@link ProjectileFirer}'s projectiles can live
         */
        public Data {
            Objects.requireNonNull(endSelectorKey, "endSelectorKey");
            Objects.requireNonNull(targetFinderKey, "targetFinderKey");
            Objects.requireNonNull(collisionFilterKey, "collisionFilterKey");
            Objects.requireNonNull(shotHandlerKeys, "shotHandlerKeys");
            Objects.requireNonNull(model, "model");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
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
