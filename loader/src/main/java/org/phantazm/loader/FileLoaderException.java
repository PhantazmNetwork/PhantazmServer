package org.phantazm.loader;

import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.ethylene.core.ConfigElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public class FileLoaderException extends IOException {
    private final Path path;
    private final ConfigElement element;
    private final ElementPath elementPath;

    private FileLoaderException(String message, Throwable cause, Path path, ConfigElement element, ElementPath elementPath) {
        super(message, cause);
        this.path = path;
        this.element = element;
        this.elementPath = elementPath;
    }

    @Override
    public String getMessage() {
        String detail = super.getMessage();
        String sep = System.lineSeparator();

        StringBuilder builder = new StringBuilder();
        builder.append(detail);

        if (path != null) {
            builder.append(sep);
            builder.append("path: ");
            builder.append(path);
        }

        if (element != null) {
            builder.append(sep);
            builder.append("element: ");
            builder.append(element);
        }

        if (elementPath != null) {
            builder.append(sep);
            builder.append("element path: ");
            builder.append(elementPath);
        }

        return builder.toString();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Path path;
        private ConfigElement element;
        private ElementPath elementPath;
        private String message;
        private Throwable cause;

        private Builder() {
        }

        public @NotNull FileLoaderException build() {
            return new FileLoaderException(message, cause, path, element, elementPath);
        }

        public @NotNull Builder withPath(@Nullable Path path) {
            this.path = path;
            return this;
        }

        public @NotNull Builder withElement(@Nullable ConfigElement element) {
            this.element = element;
            return this;
        }

        public @NotNull Builder withElementPath(@Nullable ElementPath path) {
            this.elementPath = path;
            return this;
        }

        public @NotNull Builder withMessage(@Nullable String message) {
            this.message = message;
            return this;
        }

        public @NotNull Builder withCause(@Nullable Throwable cause) {
            this.cause = cause;
            return this;
        }
    }
}
