package org.phantazm.core.instance;

import net.minestom.server.coordinate.Point;
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
import java.util.concurrent.*;

/**
 * Implements an {@link InstanceLoader} using the file system.
 */
public abstract class FileSystemInstanceLoader implements InstanceLoader {
    private final InstanceManager instanceManager;
    private final Path rootPath;

    /**
     * The {@link ChunkSupplier} to be used by instances loaded from this InstanceLoader
     */
    protected final ChunkSupplier chunkSupplier;

    private final Map<Path, InstanceContainer> instanceSources;

    private final Executor executor;

    /**
     * Creates an {@link InstanceLoader} based on a file system.
     *
     * @param rootPath      The {@link Path} of the {@link Instance} directory
     * @param chunkSupplier The {@link ChunkSupplier} used to define the chunk implementation used
     */
    public FileSystemInstanceLoader(@NotNull InstanceManager instanceManager, @NotNull Path rootPath,
            @NotNull ChunkSupplier chunkSupplier, @NotNull Executor executor) {
        this.instanceManager = Objects.requireNonNull(instanceManager, "instanceManager");
        this.rootPath = Objects.requireNonNull(rootPath, "rootPath");
        this.chunkSupplier = Objects.requireNonNull(chunkSupplier, "chunkSupplier");
        this.instanceSources = new ConcurrentHashMap<>();
        this.executor = Objects.requireNonNull(executor, "executorService");
    }

    // TODO: what if there are distinct spawnPos invocations?
    @Override
    public @NotNull CompletableFuture<Instance> loadInstance(@UnmodifiableView @NotNull List<String> subPaths) {
        Path path = rootPath;
        for (String subPath : subPaths) {
            path = path.resolve(subPath);
        }

        InstanceContainer source = instanceSources.get(path);
        if (source == null) {
            throw new IllegalArgumentException("Instance at " + path + " has not been preloaded");
        }

        return CompletableFuture.supplyAsync(source::copy, executor).thenApply(container -> {
            container.setChunkSupplier(chunkSupplier);
            instanceManager.registerInstance(container);

            return container;
        });
    }

    @Override
    public void preload(@UnmodifiableView @NotNull List<String> subPaths, @NotNull Point spawnPoint,
            int chunkViewDistance) {
        Path path = rootPath;
        for (String subPath : subPaths) {
            path = path.resolve(subPath);
        }

        instanceSources.computeIfAbsent(path, key -> {
            return createTemplateContainer(instanceManager, key, spawnPoint, chunkViewDistance);
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    private InstanceContainer createTemplateContainer(InstanceManager instanceManager, Path path, Point spawnPoint,
            int chunkViewDistance) {
        InstanceContainer container = instanceManager.createInstanceContainer(createChunkLoader(path));
        container.setChunkSupplier(chunkSupplier);

        awaitChunkLoadSync(container, spawnPoint, chunkViewDistance);
        return container;
    }

    @SuppressWarnings("UnstableApiUsage")
    private void awaitChunkLoadSync(Instance instance, Point spawnPoint, int chunkViewDistance) {
        Phaser phaser = new Phaser(1);
        ChunkUtils.forChunksInRange(spawnPoint, chunkViewDistance, (chunkX, chunkZ) -> {
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
