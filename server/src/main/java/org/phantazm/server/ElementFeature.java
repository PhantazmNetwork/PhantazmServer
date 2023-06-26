package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;

import java.util.Objects;

/**
 * Initializes features related to Element.
 */
public final class ElementFeature {
    private static ContextManager contextManager;

    static void initialize(@NotNull MappingProcessorSource mappingProcessorSource, @NotNull KeyParser keyParser) {
        Objects.requireNonNull(mappingProcessorSource, "mappingProcessorSource");
        Objects.requireNonNull(keyParser, "keyParser");

        contextManager = ContextManager.builder(Namespaces.PHANTAZM).withKeyParserFunction((ignored) -> keyParser)
                .withMappingProcessorSourceSupplier(() -> mappingProcessorSource).build();
    }

    public static @NotNull ContextManager getContextManager() {
        return FeatureUtils.check(contextManager);
    }
}
