package org.phantazm.loader;

import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.ethylene.core.collection.ConfigContainer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public interface PathExtractor {
    @NotNull List<ElementPath> paths(@NotNull ConfigContainer container) throws IOException;

    static PathExtractor constant(@NotNull ElementPath @NotNull ... path) {
        List<ElementPath> list = List.of(path);

        return new PathExtractor() {
            @Override
            public @NotNull List<ElementPath> paths(@NotNull ConfigContainer container) {
                return list;
            }
        };
    }
}
