package com.github.phantazmnetwork.commons;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class FileUtils {
    public interface IOConsumer<TType> {
        void accept(TType object) throws IOException;
    }

    public static @NotNull Path findFirstOrThrow(@NotNull Path root, @NotNull BiPredicate<Path,
            BasicFileAttributes> predicate, @NotNull Supplier<? extends String> messageSupplier)
            throws IOException {
        Path target;
        try(Stream<Path> pathStream = Files.find(root, 0, predicate)) {
            target = pathStream.findFirst().orElseThrow(() -> new IOException(messageSupplier.get()));
        }

        return target;
    }

    public static void forEachFileMatching(@NotNull Path root, @NotNull BiPredicate<Path, BasicFileAttributes> predicate,
                                           @NotNull IOConsumer<? super Path> consumer) throws IOException {
        try(Stream<Path> stream = Files.find(root, 0, predicate)) {
            for(Path path : (Iterable<? extends Path>) (stream::iterator)) {
                consumer.accept(path);
            }
        }
    }
}
