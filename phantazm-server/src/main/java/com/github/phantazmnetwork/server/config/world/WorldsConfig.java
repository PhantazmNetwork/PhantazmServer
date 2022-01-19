package com.github.phantazmnetwork.server.config.world;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

/**
 * Config for the server's worlds.
 * @param defaultWorldName The default world's name (the lobby world's name)
 * @param worldsPath The path to all worlds
 * @param mapsPath The path to all maps
 * @param worlds A map of world configuration
 */
public record WorldsConfig(@NotNull String defaultWorldName,
                           @NotNull String worldsPath,
                           @NotNull String mapsPath,
                           @NotNull Map<String, WorldConfig> worlds) {

    public WorldsConfig {
        Objects.requireNonNull(defaultWorldName, "defaultWorldName");
        Objects.requireNonNull(worldsPath, "worldsPath");
        Objects.requireNonNull(mapsPath, "mapsPath");
        Objects.requireNonNull(worlds, "worlds");
    }

}
