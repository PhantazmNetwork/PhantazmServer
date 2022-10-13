package com.github.phantazmnetwork.server;

import com.github.steanky.element.core.*;
import com.github.steanky.element.core.context.BasicContextManager;
import com.github.steanky.element.core.context.BasicElementContext;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.data.BasicDataInspector;
import com.github.steanky.element.core.data.BasicDataLocator;
import com.github.steanky.element.core.data.DataInspector;
import com.github.steanky.element.core.data.DataLocator;
import com.github.steanky.element.core.factory.BasicCollectionCreator;
import com.github.steanky.element.core.factory.BasicFactoryResolver;
import com.github.steanky.element.core.factory.CollectionCreator;
import com.github.steanky.element.core.factory.FactoryResolver;
import com.github.steanky.element.core.key.*;
import com.github.steanky.element.core.processor.BasicProcessorResolver;
import com.github.steanky.element.core.processor.ProcessorResolver;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import org.jetbrains.annotations.NotNull;

/**
 * Initializes features related to Element.
 */
public final class Element {
    private static KeyParser keyParser;
    private static ContextManager contextManager;

    static void initialize(@NotNull MappingProcessorSource mappingProcessorSource) {
        keyParser = new BasicKeyParser("phantazm");

        KeyExtractor typeExtractor = new BasicKeyExtractor("type", keyParser);
        ElementTypeIdentifier elementTypeIdentifier = new BasicElementTypeIdentifier(keyParser);
        DataInspector dataInspector = new BasicDataInspector(keyParser);
        CollectionCreator collectionCreator = new BasicCollectionCreator();

        FactoryResolver factoryResolver =
                new BasicFactoryResolver(keyParser, elementTypeIdentifier, dataInspector, collectionCreator,
                        mappingProcessorSource);

        ProcessorResolver processorResolver = new BasicProcessorResolver();
        ElementInspector elementInspector = new BasicElementInspector(factoryResolver, processorResolver);

        Registry<ConfigProcessor<?>> configRegistry = new HashRegistry<>();
        Registry<ElementFactory<?, ?>> factoryRegistry = new HashRegistry<>();
        Registry<Boolean> cacheRegistry = new HashRegistry<>();

        PathSplitter pathSplitter = BasicPathSplitter.INSTANCE;
        DataLocator dataLocator = new BasicDataLocator(pathSplitter);
        ElementContext.Source source =
                new BasicElementContext.Source(configRegistry, factoryRegistry, cacheRegistry, pathSplitter,
                        dataLocator, typeExtractor);

        contextManager = new BasicContextManager(elementInspector, elementTypeIdentifier, source);
    }

    public static @NotNull KeyParser getKeyParser() {
        return FeatureUtils.check(keyParser);
    }

    public static @NotNull ContextManager getContextManager() {
        return FeatureUtils.check(contextManager);
    }
}