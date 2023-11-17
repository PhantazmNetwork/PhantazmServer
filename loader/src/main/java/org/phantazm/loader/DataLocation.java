package org.phantazm.loader;


import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Objects;

public interface DataLocation {
    static @NotNull DataLocation path(@NotNull Path path) {
        Objects.requireNonNull(path);
        return new DataLocation() {
            @Override
            public String toString() {
                return path.toString();
            }
        };
    }
}
