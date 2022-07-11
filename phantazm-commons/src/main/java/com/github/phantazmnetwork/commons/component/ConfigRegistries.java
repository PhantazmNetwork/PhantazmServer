package com.github.phantazmnetwork.commons.component;

import com.github.phantazmnetwork.commons.ReflectionUtils;
import com.github.phantazmnetwork.commons.component.annotation.ComponentFactory;
import com.github.phantazmnetwork.commons.component.annotation.ComponentModel;
import com.github.phantazmnetwork.commons.component.annotation.ComponentProcessor;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

public final class ConfigRegistries {
    private static final KeyedConfigRegistry CONFIG_REGISTRY = new BasicKeyedConfigRegistry();
    private static final KeyedFactoryRegistry FACTORY_REGISTRY = new BasicKeyedFactoryRegistry();

    public static @NotNull KeyedConfigRegistry configRegistry() {
        return CONFIG_REGISTRY;
    }

    public static @NotNull KeyedFactoryRegistry factoryRegistry() {
        return FACTORY_REGISTRY;
    }

    public static void registerComponent(@NotNull Class<?> clazz) {
        if(clazz.getAnnotation(ComponentModel.class) == null) {
            throw new IllegalArgumentException("Class " + clazz.getTypeName() + " must have the ComponentModel " +
                    "annotation");
        }

        Predicate<Method> base = method -> {
            int modifiers = method.getModifiers();
            return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && method.getParameterCount() == 0;
        };

        Method processorMethod = ReflectionUtils.declaredMethodMatching(clazz, base.and(method -> method
                .isAnnotationPresent(ComponentProcessor.class) && KeyedConfigProcessor.class.isAssignableFrom(method
                .getReturnType())));
        if(processorMethod == null) {
            throw new IllegalArgumentException("Unable to find processor method for component class " + clazz
                    .getTypeName());
        }

        Method factoryMethod = ReflectionUtils.declaredMethodMatching(clazz, base.and(method -> method
                .isAnnotationPresent(ComponentFactory.class) && KeyedFactory.class.isAssignableFrom(method
                .getReturnType())));
        if(factoryMethod == null) {
            throw new IllegalArgumentException("Unable to find factory method for component class " + clazz
                    .getTypeName());
        }

        KeyedConfigProcessor<? extends Keyed> processor;
        KeyedFactory<?> factory;
        try {
            processor = (KeyedConfigProcessor<?>) processorMethod.invoke(null);
            if(processor == null) {
                throw new IllegalStateException("Processor method invocation returned null");
            }

            factory = (KeyedFactory<?>) factoryMethod.invoke(null);
            if(factory == null) {
                throw new IllegalStateException("Factory method invocation returned null");
            }
        }
        catch (InvocationTargetException | IllegalAccessException e) {
            //rethrow as runtime exception, we already signature-checked the methods so this shouldn't generally happen
            throw new IllegalStateException(e);
        }

        Key processorKey = processor.key();
        Key factoryKey = factory.key();
        if(!processorKey.equals(factoryKey)) {
            throw new IllegalStateException("Key used by the processor and factory cannot differ");
        }

        CONFIG_REGISTRY.registerProcessor(processor);
        FACTORY_REGISTRY.registerFactory(factory);
    }

    public static <TComponent> TComponent makeComponent(@NotNull ConfigElement element,
                                                        @NotNull DependencyProvider dependencyProvider) {
        Keyed data;
        try {
            data = CONFIG_REGISTRY.deserialize(element);
        }
        catch (ConfigProcessException e) {
            throw new IllegalArgumentException(e);
        }

        Key dataKey = data.key();
        KeyedFactory<?> factory = FACTORY_REGISTRY.getFactory(dataKey);

        if(!dependencyProvider.prepare(factory.dependencySpec())) {
            throw new IllegalStateException("Unable to prepare dependencies");
        }

        //noinspection unchecked
        return (TComponent) factory.make(dependencyProvider, data);
    }
}
