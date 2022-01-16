package com.github.zapv3.server.config.world;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record WorldsConfig(@NotNull String defaultWorldName, @NotNull String worldsPath, @NotNull String mapsPath,
                           @NotNull Map<String, WorldConfig> worlds) {

}
