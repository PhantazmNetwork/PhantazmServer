package org.phantazm.zombies.map;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.List;

/**
 * A loader for a (usually configurable) feature. The loader may load from a filesystem, a network resource, or even
 * directly from memory.
 *
 * @param <T> the type of data to load
 */
public interface Loader<T extends Keyed> {
    /**
     * Loads some data. If the data does not exist, an {@link IOException} will be thrown.
     *
     * @param dataName the name of the data to load
     * @return the data object
     * @throws IOException if the data does not exist
     */
    @NotNull T load(@NotNull String dataName) throws IOException;

    /**
     * Saves (writes) some data to this loader. This is an optional operation. Implementations may be read-only, in
     * which case this method will throw an {@link UnsupportedOperationException}.
     *
     * @param data the data to save
     * @throws IOException if an error occurs while saving the data
     */
    void save(@NotNull T data) throws IOException;

    /**
     * Deletes some data from this loader. This is an optional operation. Implementations may be read-only, in which
     * case this method will throw an {@link UnsupportedOperationException}.
     *
     * @param dataName the name of the data to delete
     * @throws IOException if the data does not exist
     */
    void delete(@NotNull String dataName) throws IOException;

    /**
     * Provides an indication of the names of the data accessible through this loader. The returned list <i>should</i>
     * include all data that may be successfully loaded, or (if not read-only) deleted. However, it is possible for
     * {@link IOException}s to still be thrown during load or removal even if they are present in this list.
     *
     * @return a list of data names indicating what should be possible to load or remove from this loader
     * @throws IOException if an error occurs while trying to determine what may be loaded
     */
    @NotNull @Unmodifiable List<String> loadableData() throws IOException;
}
