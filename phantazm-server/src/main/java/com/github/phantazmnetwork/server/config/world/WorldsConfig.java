package com.github.phantazmnetwork.server.config.world;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
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
                           @NotNull Path worldsPath,
                           @NotNull Path mapsPath,
                           @NotNull Map<String, WorldConfig> worlds) {
    /**
     * The default world name.
     */
    public static final String DEFAULT_DEFAULT_WORLD_NAME = "world";

    /**
     * The default location used to store worlds.
     */
    public static final String DEFAULT_WORLDS_PATH_STRING = "./worlds/";

    /**
     * The default location used to store maps.
     */
    public static final String DEFAULT_MAPS_PATH_STRING = "./maps/";

    /**
     * The default WorldsConfig instance.
     */
    public static final WorldsConfig DEFAULT = new WorldsConfig(DEFAULT_DEFAULT_WORLD_NAME,
            Path.of(DEFAULT_WORLDS_PATH_STRING), Path.of(DEFAULT_MAPS_PATH_STRING), Collections.emptyMap());

    /**
     * Creates config for the server's worlds.
     * @param defaultWorldName The default world's name (the lobby world's name)
     * @param worldsPath The path to all worlds
     * @param mapsPath The path to all maps
     * @param worlds A map of world configuration
     */
    public WorldsConfig {
        Objects.requireNonNull(defaultWorldName, "defaultWorldName");
        Objects.requireNonNull(worldsPath, "worldsPath");
        Objects.requireNonNull(mapsPath, "mapsPath");
        Objects.requireNonNull(worlds, "worlds");
    }

}
