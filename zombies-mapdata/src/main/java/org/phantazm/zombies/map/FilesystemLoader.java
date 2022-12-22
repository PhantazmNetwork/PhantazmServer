package org.phantazm.zombies.map;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class FilesystemLoader<T extends Keyed> implements Loader<T> {
    protected final Path root;

    public FilesystemLoader(@NotNull Path root) {
        this.root = Objects.requireNonNull(root, "root");
    }

    @Override
    public @NotNull @Unmodifiable List<String> loadableData() throws IOException {
        Files.createDirectories(root);

        try (Stream<Path> fileStream = Files.list(root)) {
            return fileStream.map(path -> path.getFileName().toString()).toList();
        }
    }
}
