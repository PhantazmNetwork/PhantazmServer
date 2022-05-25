package com.github.phantazmnetwork.zombies.map;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface MapLoader {
    @NotNull MapData load(@NotNull String mapName) throws IOException;

    void save(@NotNull MapData data, @NotNull String mapName) throws IOException;
}
