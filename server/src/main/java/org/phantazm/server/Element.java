package org.phantazm.server;

import com.github.steanky.element.core.*;
import com.github.steanky.element.core.context.BasicContextManager;
import com.github.steanky.element.core.context.BasicElementContext;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.data.BasicDataInspector;
import com.github.steanky.element.core.data.DataInspector;
import com.github.steanky.element.core.factory.BasicContainerCreator;
import com.github.steanky.element.core.factory.BasicFactoryResolver;
import com.github.steanky.element.core.factory.ContainerCreator;
import com.github.steanky.element.core.factory.FactoryResolver;
import com.github.steanky.element.core.key.*;
import com.github.steanky.element.core.processor.BasicProcessorResolver;
import com.github.steanky.element.core.processor.ProcessorResolver;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Initializes features related to Element.
 */
public final class Element {
    private static ContextManager contextManager;

    static void initialize(@NotNull MappingProcessorSource mappingProcessorSource, @NotNull KeyParser keyParser) {
        Objects.requireNonNull(mappingProcessorSource, "mappingProcessorSource");
        Objects.requireNonNull(keyParser, "keyParser");

        KeyExtractor typeExtractor = new BasicKeyExtractor("type", keyParser);
        ElementTypeIdentifier elementTypeIdentifier = new BasicElementTypeIdentifier(keyParser);
        DataInspector dataInspector = new BasicDataInspector(keyParser);
        ContainerCreator containerCreator = new BasicContainerCreator();

        FactoryResolver factoryResolver =
                new BasicFactoryResolver(keyParser, dataInspector, containerCreator, mappingProcessorSource);

        ProcessorResolver processorResolver = BasicProcessorResolver.INSTANCE;
        ElementInspector elementInspector = new BasicElementInspector(factoryResolver, processorResolver);

        Registry<ConfigProcessor<?>> configRegistry = new HashRegistry<>();
        Registry<ElementFactory<?, ?>> factoryRegistry = new HashRegistry<>();
        Registry<Boolean> cacheRegistry = new HashRegistry<>();

        ElementContext.Source source =
                new BasicElementContext.Source(configRegistry, factoryRegistry, cacheRegistry, typeExtractor);

        contextManager = new BasicContextManager(elementInspector, elementTypeIdentifier, source);
    }

    public static @NotNull ContextManager getContextManager() {
        return FeatureUtils.check(contextManager);
    }
}