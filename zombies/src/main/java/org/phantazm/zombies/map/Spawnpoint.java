package org.phantazm.zombies.map;

import com.github.steanky.vector.Vec3I;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.mob.MobModel;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.spawner.MobSpawner;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.ZombiesPlayerModule;
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
    private final Pos spawnPoint;
    private final MobSpawner mobSpawner;

    private final Window linkedWindow;
    private final Room linkedRoom;

    /**
     * Constructs a new instance of this class.
     *
     * @param spawnInfo         the backing data object
     * @param instance          the instance which this MapObject is in
     * @param spawnruleFunction the function used to resolve {@link SpawnruleInfo} data from keys
     * @param mobSpawner        the function used to actually spawn mobs in the world
     */
    public Spawnpoint(@NotNull Point mapOrigin, @NotNull SpawnpointInfo spawnInfo, @NotNull Instance instance,
            @NotNull Function<? super Key, ? extends SpawnruleInfo> spawnruleFunction, @NotNull MobSpawner mobSpawner,
            @NotNull BoundedTracker<Window> windowTracker, @NotNull BoundedTracker<Room> roomTracker) {
        this.spawnInfo = Objects.requireNonNull(spawnInfo, "spawnInfo");

        Vec3I spawnPosition = spawnInfo.position();
        this.spawnPoint = Pos.fromPoint(mapOrigin.add(spawnPosition.x(), spawnPosition.y(), spawnPosition.z()));
        this.spawnrules = Objects.requireNonNull(spawnruleFunction, "spawnrules");
        this.instance = Objects.requireNonNull(instance, "instance");
        this.mobSpawner = Objects.requireNonNull(mobSpawner, "mobSpawner");

        if (spawnInfo.linkToWindow()) {
            Vec3I linkedWindowPosition = spawnInfo.linkedWindow();
            if (linkedWindowPosition != null) {
                Optional<Window> linkedWindow = windowTracker.atPoint(linkedWindowPosition.x() + mapOrigin.blockX(),
                        linkedWindowPosition.y() + mapOrigin.blockY(), linkedWindowPosition.z() + mapOrigin.blockZ());
                if (linkedWindow.isEmpty()) {
                    LOGGER.warn(
                            "No linked window found at " + linkedWindowPosition + ", for spawnpoint at ~" + spawnPoint);
                    this.linkedWindow = null;
                }
                else {
                    this.linkedWindow = linkedWindow.get();
                }
            }
            else {
                Optional<Window> linkedWindow =
                        windowTracker.closestInRangeToBounds(spawnPoint.add(0.5, 0, 0.5), 1, 1, 10);
                if (linkedWindow.isEmpty()) {
                    LOGGER.warn("No window to link to found within 10 blocks of spawnpoint at ~" + spawnPoint);
                    this.linkedWindow = null;
                }
                else {
                    this.linkedWindow = linkedWindow.get();
                }
            }
        }
        else {
            this.linkedWindow = null;
        }

        this.linkedRoom = roomTracker.atPoint(spawnPoint).orElse(null);
        if (linkedRoom == null && linkedWindow == null) {
            LOGGER.warn("No linked room or window found for spawnpoint at ~" + spawnPoint);
        }
    }

    /**
     * Check if this spawnpoint is capable of spawning any mobs.
     *
     * @param zombiesPlayers the zombies players in the game
     * @return true if this spawnpoint can spawn at least one kind of mob; false otherwise
     */
    public boolean canSpawnAny(@NotNull Collection<? extends ZombiesPlayer> zombiesPlayers) {
        if (linkedWindow != null) {
            Optional<Room> linkedRoom = linkedWindow.getLinkedRoom();
            if (linkedRoom.isEmpty()) {
                LOGGER.warn("Linked window at ~" + linkedWindow.getCenter() + " does not have a linked room, for" +
                        " spawnpoint at ~" + spawnPoint);
                LOGGER.warn("Because of the missing link, spawning will be disallowed");
                return false;
            }

            Room room = linkedRoom.get();
            if (!room.isOpen() && !room.getRoomInfo().isSpawn()) {
                return false;
            }
        }
        else if (linkedRoom != null && !linkedRoom.isOpen() && !linkedRoom.getRoomInfo().isSpawn()) {
            return false;
        }

        Key spawnruleKey = spawnInfo.spawnRule();
        SpawnruleInfo spawnrule = spawnrules.apply(spawnruleKey);
        if (spawnrule == null) {
            LOGGER.warn("Unrecognized spawnrule " + spawnruleKey + " at " + spawnPoint + "; mob not allowed to spawn");
            return false;
        }

        double slaSquared = spawnrule.slaSquared();
        boolean inRange = false;
        for (ZombiesPlayer player : zombiesPlayers) {
            ZombiesPlayerModule module = player.module();
            if (!(module.getMeta().canTriggerSLA())) {
                continue;
            }

            Optional<Player> playerOptional = module.getPlayerView().getPlayer();
            if (playerOptional.isPresent()) {
                if (playerOptional.get().getPosition().distanceSquared(spawnPoint.add(0.5, 0, 0.5)) < slaSquared) {
                    inRange = true;
                    break;
                }
            }
        }

        return inRange;
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
        if (!canSpawnAny(zombiesPlayers)) {
            return false;
        }

        SpawnruleInfo spawnrule = spawnrules.apply(spawnInfo.spawnRule());
        if (!spawnrule.spawnType().equals(spawnType)) {
            return false;
        }

        return spawnrule.isBlacklist() != spawnrule.spawns().contains(model.key());
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
        return mobSpawner.spawn(instance, spawnPoint, model);
    }

    public @NotNull SpawnpointInfo getSpawnInfo() {
        return spawnInfo;
    }
}
