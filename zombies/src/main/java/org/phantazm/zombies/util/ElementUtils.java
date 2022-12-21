package org.phantazm.zombies.util;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

public final class ElementUtils {
    private static <T> T processIfNode(ConfigElement element, Function<? super ConfigNode, ? extends T> action,
            String elementName, Logger logger) {
        if (element.isNode()) {
            try {
                return action.apply(element.asNode());
            }
            catch (Throwable e) {
                logger.warn("Exception thrown when creating element object '" + elementName + "'", e);
            }

            return null;
        }

        logger.warn("Expected ConfigNode, was {}", element);
        return null;
    }

    public static <T> @Nullable T createElement(@NotNull ContextManager contextManager, @NotNull ConfigElement element,
            @NotNull String elementName, @NotNull DependencyProvider dependencyProvider, @NotNull Logger logger) {
        return processIfNode(element, node -> contextManager.makeContext(node).provide(dependencyProvider), elementName,
                logger);
    }

    public static @NotNull ConfigList extractList(@NotNull ConfigNode rootNode, @NotNull Logger logger,
            @NotNull Object @NotNull ... path) {
        try {
            return rootNode.getListOrThrow(path);
        }
        catch (ConfigProcessException e) {
            logger.warn("Error getting ConfigList from path '" + Arrays.toString(path) + "'", e);
        }

        return ConfigList.of();
    }

    public static <T> void createElements(@NotNull ContextManager contextManager, @NotNull ConfigList list,
            @NotNull Collection<T> collection, @NotNull String elementName,
            @NotNull DependencyProvider dependencyProvider, @NotNull Logger logger) {
        for (ConfigElement element : list) {
            processIfNode(element, node -> {
                T e = contextManager.makeContext(node).provide(dependencyProvider);
                collection.add(e);
                return e;
            }, elementName, logger);
        }
    }

    public static <T> void createElements(@NotNull ConfigList list, @NotNull String basePath,
            @NotNull ElementContext context, @NotNull Collection<T> collection, @NotNull String elementName,
            @NotNull DependencyProvider dependencyProvider, @NotNull Logger logger) {
        for (int i = 0; i < list.size(); i++) {
            ConfigElement element = list.get(i);

            int finalI = i;
            processIfNode(element, node -> {
                T e = context.provide(basePath + "/" + finalI, dependencyProvider, true);
                collection.add(e);
                return e;
            }, elementName, logger);
        }
    }
}
