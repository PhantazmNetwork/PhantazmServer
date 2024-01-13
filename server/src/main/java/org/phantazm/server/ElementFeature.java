package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.util.ElementSearcher;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;
import org.phantazm.server.context.EthyleneContext;

/**
 * Initializes features related to Element.
 */
public final class ElementFeature {
    public static final String PHANTAZM_PACKAGE = "org.phantazm";

    private static ContextManager contextManager;

    static void initialize(@NotNull EthyleneContext ethyleneContext) {
        contextManager = ContextManager.builder(Namespaces.PHANTAZM).withKeyParserFunction((ignored) -> ethyleneContext.keyParser())
            .withMappingProcessorSourceSupplier(ethyleneContext::mappingProcessorSource).build();
        contextManager.registerElementClasses(ElementSearcher.getElementClassesInPackage(PHANTAZM_PACKAGE));
    }

    public static @NotNull ContextManager getContextManager() {
        return FeatureUtils.check(contextManager);
    }
}
