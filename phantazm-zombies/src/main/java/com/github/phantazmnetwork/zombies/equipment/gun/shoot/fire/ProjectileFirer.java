package com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire;

import com.github.phantazmnetwork.api.config.processor.MinestomConfigProcessors;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint.ShotEndpointSelector;
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
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

public class ProjectileFirer implements Firer {

    public record Data(@NotNull Key endSelectorKey,
                       @NotNull Key targetFinderKey,
                       @NotNull Collection<Key> shotHandlerKeys,
                       @NotNull EntityType entityType,
                       double power,
                       double spread,
                       boolean hasGravity,
                       long maxAliveTime) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.firer.hit_scan");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor(@NotNull Collection<Key> requested) {
        Objects.requireNonNull(requested, "requested");

        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();
        ConfigProcessor<Collection<Key>> collectionProcessor = keyProcessor.collectionProcessor(ArrayList::new);
        ConfigProcessor<EntityType> entityTypeProcessor = MinestomConfigProcessors.entityType();
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key endSelectorKey = keyProcessor.dataFromElement(element.getElementOrThrow("endSelector"));
                Key targetFinderKey = keyProcessor.dataFromElement(element.getElementOrThrow("targetFinder"));
                Collection<Key> shotHandlerKeys = collectionProcessor.dataFromElement(element.getElementOrThrow("shotHandlers"));

                requested.add(endSelectorKey);
                requested.add(targetFinderKey);
                requested.addAll(shotHandlerKeys);

                EntityType entityType = entityTypeProcessor.dataFromElement(element.getElementOrThrow("entityType"));
                double power = element.getNumberOrThrow("power").doubleValue();
                double spread = element.getNumberOrThrow("spread").doubleValue();
                boolean hasGravity = element.getBooleanOrThrow("hasGravity");
                long maxAliveTime = element.getNumberOrThrow("maxAliveTime").longValue();

                return new Data(endSelectorKey, targetFinderKey, shotHandlerKeys, entityType, power, spread, hasGravity,
                        maxAliveTime);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(8);
                node.put("endSelector", keyProcessor.elementFromData(data.endSelectorKey()));
                node.put("targetFinder", keyProcessor.elementFromData(data.targetFinderKey()));
                node.put("shotHandlers", collectionProcessor.elementFromData(data.shotHandlerKeys()));
                node.put("entityType", entityTypeProcessor.elementFromData(data.entityType()));
                node.putNumber("power", data.power());
                node.putNumber("spread", data.spread());
                node.putBoolean("hasGravity", data.hasGravity());
                node.putNumber("maxAliveTime", data.maxAliveTime());

                return node;
            }
        };
    }

    private record FiredShot(@NotNull GunState state, @NotNull Pos start,
                             @NotNull Collection<PhantazmMob> previousHits) {

        private FiredShot {
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(start, "start");
            Objects.requireNonNull(previousHits, "previousHits");
        }

    }

    private record AliveProjectile(@NotNull Reference<EntityProjectile> projectile, long time) {

        private AliveProjectile {
            Objects.requireNonNull(projectile, "projectile");
        }

    }

    private final PriorityQueue<AliveProjectile> removalQueue
            = new PriorityQueue<>(Comparator.comparingLong(AliveProjectile::time));

    private final Map<UUID, FiredShot> firedShots = new HashMap<>();

    private final Data data;

    private final PlayerView playerView;

    private final ShotEndpointSelector endSelector;

    private final TargetFinder targetFinder;

    private final Collection<ShotHandler> shotHandlers;

    public ProjectileFirer(@NotNull Data data, @NotNull PlayerView playerView,
                           @NotNull ShotEndpointSelector endSelector, @NotNull TargetFinder targetFinder,
                           @NotNull Collection<ShotHandler> shotHandlers) {
        this.data = Objects.requireNonNull(data, "data");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.endSelector = Objects.requireNonNull(endSelector, "endSelector");
        this.targetFinder = Objects.requireNonNull(targetFinder, "targetFinder");
        this.shotHandlers = Objects.requireNonNull(shotHandlers, "shotHandlers");
    }

    @Override
    public void fire(@NotNull GunState state, @NotNull Pos start, @NotNull Collection<PhantazmMob> previousHits) {
        playerView.getPlayer().ifPresent(player -> {
            Instance instance = player.getInstance();
            if (instance == null) {
                return;
            }

            endSelector.getEnd(start).ifPresent(end -> {
                EntityProjectile projectile = new EntityProjectile(player, data.entityType());
                projectile.setNoGravity(!data.hasGravity());
                projectile.setInstance(instance, start).thenRun(() -> {
                    projectile.shoot(end, data.power(), data.spread());
                });

                firedShots.put(projectile.getUuid(), new FiredShot(state, start, previousHits));
                removalQueue.add(new AliveProjectile(new WeakReference<>(projectile), System.currentTimeMillis()));
            });
        });
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        for (AliveProjectile aliveProjectile = removalQueue.peek(); aliveProjectile != null
                && (time - aliveProjectile.time()) / 50 > data.maxAliveTime(); aliveProjectile = removalQueue.peek()) {
            EntityProjectile projectile = aliveProjectile.projectile().get();
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

    public void onProjectileCollision(@NotNull ProjectileCollideWithEntityEvent event) {
        if (!(event.getEntity() instanceof EntityProjectile projectile) || event.getTarget() instanceof Player) {
            return;
        }

        FiredShot firedShot = firedShots.get(projectile.getUuid());
        if (firedShot == null) {
            return;
        }

        onProjectileCollision(firedShot, projectile, event.getCollisionPosition());
    }

    private void onProjectileCollision(@NotNull FiredShot firedShot, @NotNull EntityProjectile projectile,
                                       @NotNull Point collision) {
        if (projectile.getShooter() instanceof Player player && player.getUuid().equals(playerView.getUUID())) {
            TargetFinder.Result target = targetFinder.findTarget(player, firedShot.start(),
                    collision, firedShot.previousHits());
            for (GunHit hit : target.regular()) {
                firedShot.previousHits().add(hit.mob());
            }
            for (GunHit hit : target.headshot()) {
                firedShot.previousHits().add(hit.mob());
            }
            for (ShotHandler shotHandler : shotHandlers) {
                shotHandler.handle(firedShot.state(), player, firedShot.previousHits(),
                        new GunShot(firedShot.start(), collision, target.regular(), target.headshot()));
            }
        }

        projectile.remove();
        removalQueue.removeIf(aliveProjectile -> aliveProjectile.projectile().refersTo(projectile));
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }

}
