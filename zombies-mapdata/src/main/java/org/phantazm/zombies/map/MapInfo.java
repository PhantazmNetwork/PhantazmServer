package org.phantazm.zombies.map;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Represents a Zombies map.
 */
public record MapInfo(@NotNull MapSettingsInfo settings,
                      @NotNull PlayerCoinsInfo playerCoins,
                      @NotNull List<RoomInfo> rooms,
                      @NotNull List<DoorInfo> doors,
                      @NotNull List<ShopInfo> shops,
                      @NotNull List<WindowInfo> windows,
                      @NotNull List<RoundInfo> rounds,
                      @NotNull List<SpawnruleInfo> spawnrules,
                      @NotNull List<SpawnpointInfo> spawnpoints,
                      @NotNull ConfigNode scoreboard,
                      @NotNull ConfigNode corpse) implements Keyed {
    /**
     * Constructs a new instances of this record.
     *
     * @param settings    the settings defining the general parameters for this map
     * @param rooms       this map's rooms
     * @param doors       this map's doors
     * @param shops       this map's shops
     * @param windows     this map's windows
     * @param rounds      this map's rounds
     * @param spawnrules  this map's spawnrules
     * @param spawnpoints this map's spawnpoints
     * @param scoreboard  this map's scoreboard info
     */
    public MapInfo(@NotNull MapSettingsInfo settings, @NotNull PlayerCoinsInfo playerCoins,
            @NotNull List<RoomInfo> rooms, @NotNull List<DoorInfo> doors, @NotNull List<ShopInfo> shops,
            @NotNull List<WindowInfo> windows, @NotNull List<RoundInfo> rounds, @NotNull List<SpawnruleInfo> spawnrules,
            @NotNull List<SpawnpointInfo> spawnpoints, @NotNull ConfigNode scoreboard, @NotNull ConfigNode corpse) {
        this.settings = Objects.requireNonNull(settings, "settings");
        this.playerCoins = Objects.requireNonNull(playerCoins, "playerCoins");
        this.rooms = Objects.requireNonNull(rooms, "rooms");
        this.doors = Objects.requireNonNull(doors, "doors");
        this.shops = Objects.requireNonNull(shops, "shops");
        this.windows = Objects.requireNonNull(windows, "windows");
        this.rounds = Objects.requireNonNull(rounds, "rounds");
        this.spawnrules = Objects.requireNonNull(spawnrules, "spawnrules");
        this.spawnpoints = Objects.requireNonNull(spawnpoints, "spawnpoints");
        this.scoreboard = Objects.requireNonNull(scoreboard, "scoreboard");
        this.corpse = Objects.requireNonNull(corpse, "corpse");
    }

    @Override
    public @NotNull Key key() {
        return settings.id();
    }
}
