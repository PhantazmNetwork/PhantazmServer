package org.phantazm.loader;

import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.bridge.Configuration;
import com.github.steanky.toolkit.collection.Iterators;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A source of data. Functionally equivalent to an immutable {@link Iterator} over {@link ConfigElement} objects, but
 * can throw {@link IOException} on calls to {@link DataSource#hasNext()} or {@link DataSource#next()}.
 */
public interface DataSource extends Closeable {
    boolean hasNext() throws IOException;

    @NotNull ConfigElement next() throws IOException;

    static @NotNull DataSource directory(@NotNull Path root, @NotNull ConfigCodec codec) {
        Objects.requireNonNull(root);
        Objects.requireNonNull(codec);
        return new Directory(root, codec, null, false);
    }

    static @NotNull DataSource directory(@NotNull Path root, @NotNull ConfigCodec codec, @NotNull String pathMatcher) {
        Objects.requireNonNull(root);
        Objects.requireNonNull(codec);
        Objects.requireNonNull(pathMatcher);
        return new Directory(root, codec, pathMatcher, false);
    }

    static @NotNull DataSource directory(@NotNull Path root, @NotNull ConfigCodec codec, @NotNull String pathMatcher,
        boolean symlink) {
        Objects.requireNonNull(root);
        Objects.requireNonNull(codec);
        Objects.requireNonNull(pathMatcher);
        return new Directory(root, codec, pathMatcher, symlink);
    }

    static @NotNull DataSource directory(@NotNull Path root, @NotNull ConfigCodec codec, boolean symlink) {
        Objects.requireNonNull(root);
        Objects.requireNonNull(codec);
        return new Directory(root, codec, null, symlink);
    }

    class Directory implements DataSource {
        private static final FileVisitOption[] VISIT_SYMLINKS = new FileVisitOption[]{FileVisitOption.FOLLOW_LINKS};
        private static final FileVisitOption[] NO_VISIT_SYMLINKS = new FileVisitOption[0];

        private static final LinkOption[] SYMLINK = new LinkOption[0];
        private static final LinkOption[] NO_SYMLINK = new LinkOption[]{LinkOption.NOFOLLOW_LINKS};

        private final Path root;
        private final ConfigCodec codec;
        private final PathMatcher pathMatcher;
        private final boolean symlink;

        private boolean closed;
        private Stream<Path> stream;
        private Iterator<Path> iterator;

        private Path cache;

        private Directory(Path root, ConfigCodec codec, String pathMatcher, boolean symlink) {
            this.root = root;
            this.codec = codec;
            this.pathMatcher = pathMatcher == null ? null : root.getFileSystem().getPathMatcher(pathMatcher);
            this.symlink = symlink;
        }

        private Iterator<Path> getIterator() throws FileLoaderException {
            if (iterator != null) {
                return iterator;
            }

            if (!Files.exists(root, symlink ? SYMLINK : NO_SYMLINK)) {
                return iterator = Iterators.iterator();
            }

            try {
                return iterator = (stream = Files.walk(root, symlink ? VISIT_SYMLINKS : NO_VISIT_SYMLINKS)).iterator();
            } catch (IOException e) {
                throw FileLoaderException.builder()
                    .withCause(e)
                    .withMessage("failed to initialize data stream")
                    .build();
            }
        }

        private boolean hasNext0() throws FileLoaderException {
            Iterator<Path> itr = getIterator();
            try {
                return itr.hasNext();
            } catch (UncheckedIOException e) {
                throw FileLoaderException.builder()
                    .withCause(e)
                    .withMessage("failed to access file")
                    .build();
            }
        }

        private Path next0() throws FileLoaderException {
            Iterator<Path> itr = getIterator();
            try {
                return itr.next();
            } catch (UncheckedIOException e) {
                throw FileLoaderException.builder()
                    .withCause(e)
                    .withMessage("failed to access file")
                    .build();
            }
        }

        private ConfigElement load(Path path) throws FileLoaderException {
            try {
                return Configuration.read(path, codec);
            } catch (IOException e) {
                throw FileLoaderException.builder()
                    .withCause(e)
                    .withMessage("failed to load data from file")
                    .withPath(path)
                    .build();
            }
        }

        private void validateOpen() throws FileLoaderException {
            if (closed) throw FileLoaderException.builder()
                .withMessage("this resource has been closed")
                .build();
        }

        @Override
        public boolean hasNext() throws FileLoaderException {
            validateOpen();
            if (cache != null) {
                return true;
            }

            if (pathMatcher == null) {
                return hasNext0();
            }

            while (hasNext0()) {
                Path check = next0();
                if (pathMatcher.matches(check)) {
                    cache = check;
                    return true;
                }
            }

            return false;
        }

        @Override
        public @NotNull ConfigElement next() throws FileLoaderException {
            validateOpen();
            Path cache = this.cache;
            if (cache != null) {
                this.cache = null;
                return load(cache);
            }

            if (pathMatcher == null) {
                return load(next0());
            }

            Path path;
            do {
                path = next0();
            }
            while (!pathMatcher.matches(path));

            return load(path);
        }

        @Override
        public void close() {
            closed = true;

            Stream<Path> stream = this.stream;
            if (stream != null) {
                this.stream = null;
                this.iterator = null;
                this.cache = null;

                stream.close();
            }
        }
    }
}
