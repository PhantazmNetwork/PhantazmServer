package com.github.phantazmnetwork.zombies.map;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Represents a Zombies map.
 */
public record MapInfo(@NotNull MapSettingsInfo settings,
                      @NotNull List<RoomInfo> rooms,
                      @NotNull List<DoorInfo> doors,
                      @NotNull List<ShopInfo> shops,
                      @NotNull List<WindowInfo> windows,
                      @NotNull List<RoundInfo> rounds,
                      @NotNull List<SpawnruleInfo> spawnrules,
                      @NotNull List<SpawnpointInfo> spawnpoints) {
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
     */
    public MapInfo {
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(rooms, "rooms");
        Objects.requireNonNull(doors, "doors");
        Objects.requireNonNull(shops, "shops");
        Objects.requireNonNull(windows, "windows");
        Objects.requireNonNull(rounds, "rounds");
        Objects.requireNonNull(spawnrules, "spawnrules");
        Objects.requireNonNull(spawnpoints, "spawnpoints");
    }
}
