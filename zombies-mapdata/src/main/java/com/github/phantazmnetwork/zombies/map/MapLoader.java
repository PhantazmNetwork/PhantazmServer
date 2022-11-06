package com.github.phantazmnetwork.zombies.map;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Represents a class that can save and load {@link MapInfo} objects from some data source (e.g. a file).
 */
public interface MapLoader {
    /**
     * Loads some named {@link MapInfo}.
     *
     * @param mapName the name of the map to load
     * @return the loaded map
     * @throws IOException if an IO error occurs
     */
    @NotNull MapInfo load(@NotNull String mapName) throws IOException;

    /**
     * Saves some {@link MapInfo}.
     *
     * @param data the MapInfo to save
     * @throws IOException if an IO error occurs
     */
    void save(@NotNull MapInfo data) throws IOException;

    /**
     * Deletes a named map.
     *
     * @param mapName the name of the map to delete
     * @throws IOException if an IO error occurs
     */
    void delete(@NotNull String mapName) throws IOException;
}
