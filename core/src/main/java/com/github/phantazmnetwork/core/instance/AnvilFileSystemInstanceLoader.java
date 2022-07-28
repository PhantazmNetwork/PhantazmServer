package com.github.phantazmnetwork.core.instance;

import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * A {@link FileSystemInstanceLoader} that loads {@link Instance}s using {@link AnvilLoader}s.
 */
public class AnvilFileSystemInstanceLoader extends FileSystemInstanceLoader {

    /**
     * Creates an {@link InstanceLoader} based on a file system using {@link AnvilLoader}s.
     *
     * @param rootPath      The {@link Path} of the {@link Instance} directory
     * @param chunkSupplier the {@link ChunkSupplier} used to create chunks
     */
    public AnvilFileSystemInstanceLoader(@NotNull Path rootPath, @NotNull ChunkSupplier chunkSupplier) {
        super(rootPath, chunkSupplier);
    }

    @Override
    protected @NotNull IChunkLoader createChunkLoader(@NotNull Path path) {
        AnvilLoader loader = new AnvilLoader(path);
        loader.setChunkSupplier(chunkSupplier);
        return loader;
    }

}
