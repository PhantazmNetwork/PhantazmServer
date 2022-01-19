package com.github.phantazmnetwork.api.world;

import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * Implements a {@link WorldLoader} using the file system.
 */
@SuppressWarnings("ClassCanBeRecord")
public class FileSystemWorldLoader implements WorldLoader {

    private final Path rootPath;

    private final Function<Path, IChunkLoader> loaderCreator;

    public FileSystemWorldLoader(@NotNull Path rootPath, @NotNull Function<Path, IChunkLoader> loaderCreator) {
        this.rootPath = rootPath;
        this.loaderCreator = loaderCreator;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull Instance loadWorld(@NotNull InstanceManager instanceManager, @NotNull String worldName) {
        Path path = rootPath.resolve(worldName);
        return instanceManager.createInstanceContainer(loaderCreator.apply(path));
    }

}
