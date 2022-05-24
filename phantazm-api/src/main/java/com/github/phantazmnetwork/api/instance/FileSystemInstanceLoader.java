package com.github.phantazmnetwork.api.instance;

import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Implements an {@link InstanceLoader} using the file system.
 */
public abstract class FileSystemInstanceLoader implements InstanceLoader {

    private final Path rootPath;
    protected final ChunkSupplier chunkSupplier;

    /**
     * Creates an {@link InstanceLoader} based on a file system.
     * @param rootPath The {@link Path} of the {@link Instance} directory
     * @param chunkSupplier The {@link ChunkSupplier} used to define the chunk implementation used
     */
    public FileSystemInstanceLoader(@NotNull Path rootPath, @NotNull ChunkSupplier chunkSupplier) {
        this.rootPath = Objects.requireNonNull(rootPath, "rootPath");
        this.chunkSupplier = Objects.requireNonNull(chunkSupplier, "chunkSupplier");
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull Instance loadInstance(@NotNull InstanceManager instanceManager,
                                          @UnmodifiableView @NotNull List<String> subPaths) {
        Path path = rootPath;
        for (String subPath : subPaths) {
            path = path.resolve(subPath);
        }

        InstanceContainer container = instanceManager.createInstanceContainer(createChunkLoader(path));
        container.setChunkSupplier(chunkSupplier);
        return container;
    }

    /**
     * Creates an {@link IChunkLoader} based on a {@link Path}.
     * @param path The {@link Path} where the {@link IChunkLoader} should be created from
     * @return The new {@link IChunkLoader}
     */
    protected abstract @NotNull IChunkLoader createChunkLoader(@NotNull Path path);

}
