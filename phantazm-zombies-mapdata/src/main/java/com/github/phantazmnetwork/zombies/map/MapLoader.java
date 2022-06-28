package com.github.phantazmnetwork.zombies.map;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface MapLoader {
    @NotNull MapInfo load(@NotNull String mapName) throws IOException;

    void save(@NotNull MapInfo data) throws IOException;
}
