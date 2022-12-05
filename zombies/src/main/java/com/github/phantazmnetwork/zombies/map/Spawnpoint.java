package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.core.VecUtils;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.steanky.vector.Vec3I;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a particular position where {@link PhantazmMob} instances may be spawned.
 */
public class Spawnpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(Spawnpoint.class);
    private final SpawnpointInfo spawnInfo;
    private final Function<? super Key, ? extends SpawnruleInfo> spawnrules;
    private final Instance instance;
    private final Point spawnPoint;
    private final MobStore mobStore;
    private final MobSpawner mobSpawner;

    /**
     * Constructs a new instance of this class.
     *
     * @param spawnInfo         the backing data object
     * @param instance          the instance which this MapObject is in
     * @param spawnruleFunction the function used to resolve {@link SpawnruleInfo} data from keys
     * @param mobSpawner        the function used to actually spawn mobs in the world
     */
    public Spawnpoint(@NotNull Vec3I mapOrigin, @NotNull SpawnpointInfo spawnInfo, @NotNull Instance instance,
            @NotNull Function<? super Key, ? extends SpawnruleInfo> spawnruleFunction, @NotNull MobStore mobStore,
            @NotNull MobSpawner mobSpawner) {
        this.spawnInfo = Objects.requireNonNull(spawnInfo, "spawnInfo");
        this.spawnPoint = VecUtils.toPoint(mapOrigin.add(spawnInfo.position()));
        this.spawnrules = Objects.requireNonNull(spawnruleFunction, "spawnrules");
        this.instance = Objects.requireNonNull(instance, "instance");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.mobSpawner = Objects.requireNonNull(mobSpawner, "mobSpawner");
    }

    /**
     * Determines if this spawnpoint may spawn a {@link MobModel}.
     *
     * @param model          the model to spawn
     * @param spawnType      the spawntype, which must match the spawnrule's spawn type
     * @param zombiesPlayers the players in the map
     * @return true if the mob can spawn, false otherwise
     */
    public boolean canSpawn(@NotNull MobModel model, @NotNull Key spawnType,
            @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(spawnType, "spawnType");

        Key spawnruleKey = spawnInfo.spawnRule();
        SpawnruleInfo spawnrule = spawnrules.apply(spawnruleKey);

        if (spawnrule == null) {
            LOGGER.warn("Unrecognized spawnrule " + spawnruleKey + " at " + spawnInfo.position() + "; mob not allowed" +
                    " to " + "spawn");
            return false;
        }

        if (!spawnrule.spawnType().equals(spawnType)) {
            return false;
        }

        double slaSquared = spawnrule.slaSquared();
        boolean inRange = false;
        for (ZombiesPlayer player : zombiesPlayers) {
            if (!(player.getModule().getMeta().canTriggerSLA())) {
                continue;
            }

            Optional<Player> playerOptional = player.getModule().getPlayerView().getPlayer();
            if (playerOptional.isPresent()) {
                if (VecUtils.toDouble(playerOptional.get().getPosition())
                        .distanceSquaredTo(this.spawnInfo.position().toImmutableDouble()) < slaSquared) {
                    inRange = true;
                    break;
                }
            }
        }

        return inRange && (spawnrule.isBlacklist() != spawnrule.spawns().contains(model.key()));
    }

    /**
     * Spawns the mob at the spawnpoint, regardless of if the mob should be able to spawn here. Query
     * {@link Spawnpoint#canSpawn(MobModel, Key, Collection)} to determine if the mob should be able to spawn.
     *
     * @param model the model of the mob to spawn
     * @return the resulting {@link PhantazmMob} instance
     */
    public @NotNull PhantazmMob spawn(@NotNull MobModel model) {
        Objects.requireNonNull(model, "model");
        return mobSpawner.spawn(instance, spawnPoint, mobStore, model);
    }
}
