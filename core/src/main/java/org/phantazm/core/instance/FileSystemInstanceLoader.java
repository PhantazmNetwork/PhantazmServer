package org.phantazm.core.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkSupplier;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.jglrxavpok.hephaistos.nbt.*;
import org.phantazm.commons.FileUtils;
import org.phantazm.commons.FutureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import relocated.com.github.steanky.toolkit.function.ExceptionHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Phaser;
import java.util.stream.Stream;
import java.util.zip.*;

/**
 * Implements an {@link InstanceLoader} using the file system.
 */
public abstract class FileSystemInstanceLoader implements InstanceLoader {
    public static final int CACHE_COMPRESSION_LEVEL = 4;
    public static final Path CACHE_PATH = Path.of("./cache");

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemInstanceLoader.class);

    private static class InstanceDataChunkLoader implements IChunkLoader {
        private InstanceData instanceData;

        private InstanceDataChunkLoader(InstanceData instanceData) {
            this.instanceData = instanceData;
        }

        @Override
        public @NotNull CompletableFuture<Chunk> loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
            InstanceData instanceData = this.instanceData;
            if (instanceData == null) {
                return FutureUtils.nullCompletedFuture();
            }

            long chunkKey = ChunkUtils.getChunkIndex(chunkX, chunkZ);
            InstanceData.ChunkData chunkData = instanceData.chunkData().get(chunkKey);
            if (chunkData == null) {
                return FutureUtils.nullCompletedFuture();
            }

            Int2ObjectMap<InstanceData.NBTBlock> blockData = chunkData.entries();
            Int2ObjectMap<Block> blocks = new Int2ObjectOpenHashMap<>(blockData.size());
            for (Int2ObjectMap.Entry<InstanceData.NBTBlock> entry : blockData.int2ObjectEntrySet()) {
                int key = entry.getIntKey();
                InstanceData.NBTBlock nbtData = entry.getValue();

                Block block = Block.fromBlockId(nbtData.id());
                if (block == null) {
                    continue;
                }

                NBT nbt;
                try (NBTReader nbtReader = NBTReader.fromArray(nbtData.nbt())) {
                    nbt = nbtReader.readNamed().component2();
                } catch (IOException | NBTException e) {
                    continue;
                }

                blocks.put(key, nbt instanceof NBTCompound compound ? block.withNbt(compound) : block);
            }

            return CompletableFuture.completedFuture(new DynamicChunk(instance, chunkX, chunkZ, chunkData.sections(),
                blocks));
        }

        @Override
        public @NotNull CompletableFuture<Void> saveChunk(@NotNull Chunk chunk) {
            return FutureUtils.nullCompletedFuture();
        }

        private void clean() {
            instanceData = null;
        }
    }

    /**
     * The {@link ChunkSupplier} to be used by instances loaded from this InstanceLoader
     */
    protected final ChunkSupplier chunkSupplier;
    private final InstanceManager instanceManager;
    private final Path rootPath;
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
        this.instanceManager = Objects.requireNonNull(instanceManager);
        this.rootPath = Objects.requireNonNull(rootPath);
        this.chunkSupplier = Objects.requireNonNull(chunkSupplier);
        this.instanceSources = new ConcurrentHashMap<>();
        this.executor = Objects.requireNonNull(executor);
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private String cacheKey(Path mapPath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            Path path = mapPath.normalize();

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < path.getNameCount(); i++) {
                builder.append(path.getName(i));
                builder.append('/');
            }

            md.update(builder.toString().getBytes(StandardCharsets.UTF_8));
            return bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("Exception when calculating instance cache key", e);
        }

        return null;
    }

    private Path rootRelative(List<String> subPaths) {
        Path path = rootPath;
        for (String subPath : subPaths) {
            path = path.resolve(subPath);
        }

        return path;
    }

    // TODO: what if there are distinct spawnPos invocations?
    @Override
    public @NotNull CompletableFuture<Instance> loadInstance(@UnmodifiableView @NotNull List<String> subPaths) {
        Path path = rootRelative(subPaths);

        InstanceContainer source = instanceSources.get(path);
        if (source == null) {
            throw new IllegalArgumentException("Instance at " + path + " has not been preloaded");
        }

        return CompletableFuture.supplyAsync(source::copy, executor).thenApply(container -> {
            container.enableAutoChunkLoad(false);
            container.setChunkSupplier(chunkSupplier);
            instanceManager.registerInstance(container);

            return container;
        });
    }

    public static void clearCache() throws IOException {
        try (Stream<Path> paths = Files.list(CACHE_PATH);
             ExceptionHandler<IOException> handler = new ExceptionHandler<>(IOException.class)) {
            Iterator<Path> pathIterator = paths.iterator();
            while (pathIterator.hasNext()) {
                Path path = pathIterator.next();
                if (!Files.isDirectory(path)) {
                    handler.run(() -> Files.delete(path));
                }
            }
        }
    }

    @Override
    public void clearPreloadedInstances() {
        instanceSources.clear();
    }

    @Override
    public void preload(@UnmodifiableView @NotNull List<String> subPaths, @NotNull Point spawnPoint,
        int chunkViewDistance) {
        Path path = rootPath;
        for (String subPath : subPaths) {
            path = path.resolve(subPath);
        }

        Path finalPath = path;
        instanceSources.computeIfAbsent(finalPath, key -> {
            String cacheKey = cacheKey(finalPath);

            boolean writeCache = false;
            Path cachePath = null;
            if (cacheKey != null) {
                FileUtils.ensureDirectories(CACHE_PATH);
                cachePath = CACHE_PATH.resolve(cacheKey);
                if (Files.exists(cachePath)) {
                    try {
                        InstanceContainer instance = loadFromCache(cachePath, spawnPoint, chunkViewDistance);
                        LOGGER.info("Loaded {} from cache", finalPath);
                        return instance;
                    } catch (IOException | ClassNotFoundException e) {
                        LOGGER.warn("Exception loading instance from cache", e);
                    }
                } else {
                    writeCache = true;
                }
            }

            InstanceContainer container = createTemplateContainer(key, spawnPoint, chunkViewDistance);
            if (writeCache) {
                try {
                    FileUtils.ensureDirectories(CACHE_PATH);
                    writeCache(cachePath, container);
                    LOGGER.info("Wrote {} to cache", finalPath);
                } catch (IOException e) {
                    LOGGER.warn("Error writing instance to cache", e);
                }
            }

            return container;
        });
    }

    private void writeCache(Path cacheFile, Instance instance) throws IOException {
        Deflater deflater = new Deflater(CACHE_COMPRESSION_LEVEL);
        try (ObjectOutputStream oos = new ObjectOutputStream(new DeflaterOutputStream(
            new BufferedOutputStream(Files.newOutputStream(cacheFile, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)), deflater))) {
            oos.writeObject(InstanceData.of(instance));
        } finally {
            deflater.end();
        }
    }

    private InstanceContainer loadFromCache(Path cacheFile, Point spawnPoint, int chunkViewDistance) throws IOException,
        ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new InflaterInputStream(
            new BufferedInputStream(Files.newInputStream(cacheFile,
                StandardOpenOption.READ))))) {
            InstanceData instanceData = (InstanceData) ois.readObject();

            InstanceDataChunkLoader loader = new InstanceDataChunkLoader(instanceData);
            try {
                InstanceContainer container = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD, loader);
                container.enableAutoChunkLoad(false);
                container.setChunkSupplier(chunkSupplier);

                awaitChunkLoadSync(container, spawnPoint, chunkViewDistance);
                return container;
            } finally {
                loader.clean();
            }
        }
    }

    private InstanceContainer createTemplateContainer(Path path, Point spawnPoint, int chunkViewDistance) {
        InstanceContainer container =
            new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD, createChunkLoader(path));
        container.enableAutoChunkLoad(false);
        container.setChunkSupplier(chunkSupplier);

        awaitChunkLoadSync(container, spawnPoint, chunkViewDistance);
        return container;
    }

    private void awaitChunkLoadSync(Instance instance, Point spawnPoint, int chunkViewDistance) {
        Phaser phaser = new Phaser(1);
        ChunkUtils.forChunksInRange(spawnPoint, chunkViewDistance, (chunkX, chunkZ) -> {
            phaser.register();
            instance.loadChunk(chunkX, chunkZ).whenComplete((chunk, throwable) -> {
                phaser.arriveAndDeregister();
                if (throwable != null) {
                    LOGGER.info("Error loading chunk at ({}, {})", chunkX, chunkZ, throwable);
                }
            });
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
