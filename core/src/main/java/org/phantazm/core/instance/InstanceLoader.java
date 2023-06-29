package org.phantazm.core.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Loads usable {@link Instance}s.
 */
public interface InstanceLoader {

    /**
     * Loads an {@link Instance}.
     *
     * @param subPaths Paths used to identify the {@link Instance}
     * @return A new {@link Instance}
     */
    @NotNull CompletableFuture<Instance> loadInstance(@UnmodifiableView @NotNull List<String> subPaths);

    void preload(@UnmodifiableView @NotNull List<String> subPaths, @NotNull Point spawnPos, int chunkViewDistance);

}
