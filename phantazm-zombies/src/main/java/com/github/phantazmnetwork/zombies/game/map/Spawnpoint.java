package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.api.VecUtils;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.zombies.map.SpawnpointInfo;
import com.github.phantazmnetwork.zombies.map.SpawnruleInfo;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a particular position where {@link PhantazmMob} instances may be spawned.
 */
public class Spawnpoint extends MapObject<SpawnpointInfo> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Spawnpoint.class);
    private final Function<Key, SpawnruleInfo> spawnrules;
    private final MobSpawner mobSpawner;

    /**
     * Constructs a new instance of this class.
     *
     * @param spawnInfo the backing data object
     * @param origin    the origin vector this object's coordinates are considered relative to
     * @param instance  the instance which this MapObject is in
     * @param spawnruleFunction the function used to resolve {@link SpawnruleInfo} data from keys
     * @param mobSpawner the function used to actually spawn mobs in the world
     */
    public Spawnpoint(@NotNull SpawnpointInfo spawnInfo, @NotNull Vec3I origin, @NotNull Instance instance,
                      @NotNull Function<Key, SpawnruleInfo> spawnruleFunction, @NotNull MobSpawner mobSpawner) {
        super(spawnInfo, origin, instance);
        this.spawnrules = Objects.requireNonNull(spawnruleFunction, "spawnrules");
        this.mobSpawner = Objects.requireNonNull(mobSpawner, "mobSpawner");
    }

    /**
     * Determines if this spawnpoint may spawn a {@link MobModel}.
     * @param model the model to spawn
     * @param spawnType the spawntype, current unused
     * @return true if the mob can spawn, false otherwise
     */
    public boolean canSpawn(@NotNull MobModel model, @NotNull Key spawnType) {
        //TODO: spawnType key currently not used

        Objects.requireNonNull(model, "model");

        Key spawnruleKey = data.spawnRule();
        SpawnruleInfo spawnrule = spawnrules.apply(spawnruleKey);
        if(spawnrule == null) {
            LOGGER.warn("Unrecognized spawnrule " + spawnruleKey + " at " + data.position() + "; mob allowed to spawn");
            return true;
        }

        return spawnrule.isBlacklist() != spawnrule.spawns().contains(model.key());
    }

    /**
     * Spawns the mob at the spawnpoint, regardless of if the mob should be able to spawn here. Query
     * {@link Spawnpoint#canSpawn(MobModel, Key)} to determine if the mob should be able to spawn.
     * @param model the model of the mob to spawn
     * @return the resulting {@link PhantazmMob} instance
     */
    public @NotNull PhantazmMob spawn(@NotNull MobModel model) {
        Objects.requireNonNull(model, "model");
        return mobSpawner.spawn(instance, VecUtils.toPoint(origin.add(data.position())), model);
    }
}
