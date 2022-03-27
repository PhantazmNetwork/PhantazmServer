package com.github.phantazmnetwork.api.instance;

import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * A {@link FileSystemInstanceLoader} that loads {@link Instance}s using {@link AnvilLoader}s.
 */
public class AnvilFileSystemInstanceLoader extends FileSystemInstanceLoader {

    /**
     * Creates an {@link InstanceLoader} based on a file system using {@link AnvilLoader}s.
     * @param rootPath The {@link Path} of the {@link Instance} directory
     */
    public AnvilFileSystemInstanceLoader(@NotNull Path rootPath) {
        super(rootPath);
    }

    @Override
    protected @NotNull IChunkLoader createChunkLoader(@NotNull Path path) {
        return new AnvilLoader(path);
    }

}
