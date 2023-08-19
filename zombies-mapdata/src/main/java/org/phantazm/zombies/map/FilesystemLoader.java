package org.phantazm.zombies.map;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.FileUtils;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class FilesystemLoader<T extends Keyed> implements Loader<T> {
    protected final Path root;

    public FilesystemLoader(@NotNull Path root) {
        this.root = Objects.requireNonNull(root);
    }

    @Override
    public @NotNull
    @Unmodifiable List<String> loadableData() throws IOException {
        FileUtils.createDirectories(root);

        try (Stream<Path> fileStream = Files.walk(root, FileVisitOption.FOLLOW_LINKS).filter(Files::isRegularFile)) {
            return fileStream.map(path -> root.relativize(path).toString()).toList();
        }
    }
}
