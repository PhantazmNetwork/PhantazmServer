package org.phantazm.loader;

import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.ethylene.core.ConfigElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class LoaderException extends IOException {
    private final DataLocation location;
    private final ConfigElement element;
    private final ElementPath elementPath;
    private final String stage;

    private LoaderException(String message, Throwable cause, DataLocation location, ConfigElement element,
        ElementPath elementPath, String stage) {
        super(message, cause);
        this.location = location;
        this.element = element;
        this.elementPath = elementPath;
        this.stage = stage;
    }

    public @NotNull Builder toBuilder() {
        return builder()
            .withMessage(getMessage())
            .withCause(getCause())
            .withDataLocation(location)
            .withElement(element)
            .withElementPath(elementPath)
            .withStage(stage);
    }

    @Override
    public String getMessage() {
        String detail = super.getMessage();
        String sep = System.lineSeparator();

        StringBuilder builder = new StringBuilder();
        builder.append(detail);

        if (stage != null) {
            builder.append(sep);
            builder.append("stage: ");
            builder.append(stage);
        }

        if (location != null) {
            builder.append(sep);
            builder.append("location: ");
            builder.append(location);
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
        private DataLocation location;
        private ConfigElement element;
        private ElementPath elementPath;
        private String message;
        private String stage;
        private Throwable cause;

        private Builder() {
        }

        public @NotNull LoaderException build() {
            return new LoaderException(message, cause, location, element, elementPath, stage);
        }

        public @NotNull Builder withDataLocation(@Nullable DataLocation location) {
            this.location = location;
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

        public @NotNull Builder withStage(@Nullable String stage) {
            this.stage = stage;
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
