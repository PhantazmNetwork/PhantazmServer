package com.github.phantazmnetwork.api.instance;

import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Implements an {@link InstanceLoader} using the file system.
 */
@SuppressWarnings("ClassCanBeRecord")
public class FileSystemInstanceLoader implements InstanceLoader {

    private final Path rootPath;

    private final Function<Path, IChunkLoader> loaderCreator;

    /**
     * Creates an {@link InstanceLoader} based on a file system.
     * @param rootPath The {@link Path} of the {@link Instance} directory
     * @param loaderCreator A creator for {@link Instance}s
     */
    public FileSystemInstanceLoader(@NotNull Path rootPath, @NotNull Function<Path, IChunkLoader> loaderCreator) {
        this.rootPath = Objects.requireNonNull(rootPath, "rootPath");
        this.loaderCreator = Objects.requireNonNull(loaderCreator, "loaderCreator");
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull Instance loadInstance(@NotNull InstanceManager instanceManager,
                                          @UnmodifiableView @NotNull List<String> subPaths) {
        Path path = rootPath;
        for (String subPath : subPaths) {
            path = path.resolve(subPath);
        }

        return instanceManager.createInstanceContainer(loaderCreator.apply(path));
    }

}
