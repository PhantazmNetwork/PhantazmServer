package org.phantazm.core.instance;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.utils.chunk.ChunkSupplier;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Phaser;

/**
 * Implements an {@link InstanceLoader} using the file system.
 */
public abstract class FileSystemInstanceLoader implements InstanceLoader {

    /**
     * The {@link ChunkSupplier} to be used by instances loaded from this InstanceLoader
     */
    protected final ChunkSupplier chunkSupplier;
    private final Path rootPath;

    private final Map<Path, InstanceContainer> instanceSources;

    /**
     * Creates an {@link InstanceLoader} based on a file system.
     *
     * @param rootPath      The {@link Path} of the {@link Instance} directory
     * @param chunkSupplier The {@link ChunkSupplier} used to define the chunk implementation used
     */
    public FileSystemInstanceLoader(@NotNull Path rootPath, @NotNull ChunkSupplier chunkSupplier) {
        this.rootPath = Objects.requireNonNull(rootPath, "rootPath");
        this.chunkSupplier = Objects.requireNonNull(chunkSupplier, "chunkSupplier");
        this.instanceSources = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull Instance loadInstance(@NotNull InstanceManager instanceManager,
            @UnmodifiableView @NotNull List<String> subPaths, @NotNull Pos spawnPos, int chunkViewDistance) {
        Path path = rootPath;
        for (String subPath : subPaths) {
            path = path.resolve(subPath);
        }

        InstanceContainer containerSource = instanceSources.computeIfAbsent(path, key -> {
            InstanceContainer container = instanceManager.createInstanceContainer(createChunkLoader(key));
            container.setChunkSupplier(chunkSupplier);

            awaitChunkLoad(container, spawnPos, chunkViewDistance);
            return container;
        });

        InstanceContainer container = containerSource.copy();
        container.setChunkSupplier(chunkSupplier);
        instanceManager.registerInstance(container);

        return container;
    }

    private void awaitChunkLoad(Instance instance, Pos spawnPos, int chunkViewDistance) {
        Phaser phaser = new Phaser(1);
        ChunkUtils.forChunksInRange(spawnPos, chunkViewDistance, (chunkX, chunkZ) -> {
            phaser.register();
            instance.loadOptionalChunk(chunkX, chunkZ).whenComplete((chunk, throwable) -> phaser.arriveAndDeregister());
        });
        phaser.arriveAndAwaitAdvance();
    }

    /**
     * Creates an {@link IChunkLoader} based on a {@link Path}.
     *
     * @param path The {@link Path} where the {@link IChunkLoader} should be created from
     * @return The new {@link IChunkLoader}
     */
    protected abstract @NotNull IChunkLoader createChunkLoader(@NotNull Path path);

}
