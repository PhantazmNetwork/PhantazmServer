package com.github.phantazmnetwork.commons.component;

import com.github.phantazmnetwork.commons.ReflectionUtils;
import com.github.phantazmnetwork.commons.component.annotation.ComponentFactory;
import com.github.phantazmnetwork.commons.component.annotation.ComponentModel;
import com.github.phantazmnetwork.commons.component.annotation.ComponentProcessor;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Predicate;

public class ReflectiveComponentBuilder implements ComponentBuilder {
    private final KeyedConfigRegistry configRegistry;
    private final KeyedFactoryRegistry factoryRegistry;

    public ReflectiveComponentBuilder(@NotNull KeyedConfigRegistry configRegistry,
                                      @NotNull KeyedFactoryRegistry factoryRegistry) {
        this.configRegistry = Objects.requireNonNull(configRegistry, "configRegistry");
        this.factoryRegistry = Objects.requireNonNull(factoryRegistry, "factoryRegistry");
    }

    @Override
    public void registerComponentClass(@NotNull Class<?> component) {
        if(component.getAnnotation(ComponentModel.class) == null) {
            throw new IllegalArgumentException("Class " + component.getTypeName() + " must have the ComponentModel " +
                    "annotation");
        }

        Predicate<Method> base = method -> {
            int modifiers = method.getModifiers();
            return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && method.getParameterCount() == 0;
        };

        Method processorMethod = ReflectionUtils.declaredMethodMatching(component, base.and(method -> method
                .isAnnotationPresent(ComponentProcessor.class) && KeyedConfigProcessor.class.isAssignableFrom(method
                .getReturnType())));
        if(processorMethod == null) {
            throw new IllegalArgumentException("Unable to find processor method for component class " + component
                    .getTypeName());
        }

        Method factoryMethod = ReflectionUtils.declaredMethodMatching(component, base.and(method -> method
                .isAnnotationPresent(ComponentFactory.class) && KeyedFactory.class.isAssignableFrom(method
                .getReturnType())));
        if(factoryMethod == null) {
            throw new IllegalArgumentException("Unable to find factory method for component class " + component
                    .getTypeName());
        }

        KeyedConfigProcessor<? extends Keyed> processor;
        KeyedFactory<?, ?> factory;
        try {
            processor = (KeyedConfigProcessor<?>) processorMethod.invoke(null);
            if(processor == null) {
                throw new IllegalStateException("Processor method invocation returned null");
            }

            factory = (KeyedFactory<?, ?>) factoryMethod.invoke(null);
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

        configRegistry.registerProcessor(processor);
        factoryRegistry.registerFactory(factory);
    }

    @Override
    public <TData extends Keyed, TComponent> TComponent makeComponent(@NotNull ConfigNode node,
                                                                      @NotNull DependencyProvider provider) {
        Keyed data;
        try {
            data = configRegistry.deserialize(node);
        }
        catch (ConfigProcessException e) {
            throw new IllegalArgumentException(e);
        }

        Key dataKey = data.key();
        KeyedFactory<TData, ?> factory = factoryRegistry.getFactory(dataKey);

        if(!provider.prepare(factory.dependencySpec())) {
            throw new IllegalStateException("Unable to prepare dependencies");
        }

        //noinspection unchecked
        return (TComponent) factory.make(provider, (TData) data);
    }
}
