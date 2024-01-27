package org.phantazm.core.instance;

import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

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
    public AnvilFileSystemInstanceLoader(@NotNull InstanceManager instanceManager, @NotNull Path rootPath,
        @NotNull ChunkSupplier chunkSupplier, @NotNull Executor executor) {
        super(instanceManager, rootPath, chunkSupplier, executor);
    }

    @Override
    protected void writeCache(Path cacheFile, Path anvilFolder, Instance instance) throws IOException {
        super.writeCache(cacheFile, anvilFolder, instance);

        updateHistory(anvilFolder, historyPath(cacheFile));
    }

    @Override
    protected boolean recalculateCacheFor(@NotNull Path sourcePath, @NotNull Path cachePath) {
        Path historyPath = historyPath(cachePath);
        long lastUpdated = readLastUpdated(historyPath);
        if (lastUpdated == -1) {
            updateHistory(sourcePath, historyPath);
            return false;
        }

        return readLastUpdatedFromAnvil(sourcePath) > lastUpdated;
    }

    private static Path historyPath(Path cachePath) {
        return cachePath.resolveSibling(cachePath.getFileName().toString() + "_history");
    }

    private static long readLastUpdatedFromAnvil(Path anvilPath) {
        Path regionFolder = anvilPath.resolve("region");
        try (Stream<Path> pathStream = Files.list(regionFolder)) {
            Iterator<Path> pathIterator = pathStream.iterator();

            long mostRecent = Long.MIN_VALUE;
            while (pathIterator.hasNext()) {
                Path regionFile = pathIterator.next();
                FileTime lastModified = Files.getLastModifiedTime(regionFile);
                long modifiedMillis = lastModified.toMillis();
                if (modifiedMillis > mostRecent) {
                    mostRecent = modifiedMillis;
                }
            }

            return mostRecent;
        } catch (IOException ignored) {

        }

        return -1;
    }

    private static void updateHistory(Path anvilPath, Path historyPath) {
        long time = readLastUpdatedFromAnvil(anvilPath);
        if (time != -1) {
            writeLastUpdated(historyPath, time);
        }
    }

    private static void writeLastUpdated(Path historyPath, long time) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(historyPath,
            StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)))) {
            oos.writeLong(time);
        } catch (IOException ignored) {
        }
    }

    private static long readLastUpdated(Path historyPath) {
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(historyPath,
            StandardOpenOption.READ)))) {
            return ois.readLong();
        } catch (IOException | ClassCastException ignored) {
            return -1;
        }
    }

    @Override
    protected @NotNull IChunkLoader createChunkLoader(@NotNull Path path) {
        AnvilLoader loader = new AnvilLoader(path);
        loader.setChunkSupplier(chunkSupplier);
        return loader;
    }

}
