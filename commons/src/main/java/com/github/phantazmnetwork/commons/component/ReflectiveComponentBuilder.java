package com.github.phantazmnetwork.commons.component;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.ReflectionUtils;
import com.github.phantazmnetwork.commons.component.annotation.ComponentFactory;
import com.github.phantazmnetwork.commons.component.annotation.ComponentModel;
import com.github.phantazmnetwork.commons.component.annotation.ComponentProcessor;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Standard reflection-based implementation of {@link ComponentBuilder}. This is the main entrypoint of the component
 * framework.
 */
public class ReflectiveComponentBuilder implements ComponentBuilder {
    private static final Predicate<Method> BASE = method -> {
        int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && method.getParameterCount() == 0;
    };

    private final KeyedConfigRegistry configRegistry;
    private final KeyedFactoryRegistry factoryRegistry;

    /**
     * Creates a new instance of this class which will use the provided registries to handle component creation.
     *
     * @param configRegistry  the {@link KeyedConfigRegistry} used to register configuration processors
     * @param factoryRegistry the {@link KeyedFactoryRegistry} used to register component factories
     */
    public ReflectiveComponentBuilder(@NotNull KeyedConfigRegistry configRegistry,
                                      @NotNull KeyedFactoryRegistry factoryRegistry) {
        this.configRegistry = Objects.requireNonNull(configRegistry, "configRegistry");
        this.factoryRegistry = Objects.requireNonNull(factoryRegistry, "factoryRegistry");
    }

    @Override
    public void registerComponentClass(@NotNull Class<?> component) throws ComponentException {
        ComponentModel annotation = component.getAnnotation(ComponentModel.class);
        if (annotation == null) {
            throw new ComponentException(
                    "Class " + component.getTypeName() + " must have the ComponentModel annotation");
        }

        Method processorMethod = ReflectionUtils.declaredMethodMatching(component, BASE.and(
                method -> method.isAnnotationPresent(ComponentProcessor.class) &&
                          KeyedConfigProcessor.class.isAssignableFrom(method.getReturnType())));
        if (processorMethod == null) {
            throw new ComponentException("Unable to find processor method for " + component.getTypeName());
        }

        Method factoryMethod = ReflectionUtils.declaredMethodMatching(component, BASE.and(
                method -> method.isAnnotationPresent(ComponentFactory.class) &&
                          KeyedFactory.class.isAssignableFrom(method.getReturnType())));
        if (factoryMethod == null) {
            throw new ComponentException("Unable to find factory method for " + component.getTypeName());
        }

        KeyedConfigProcessor<? extends Keyed> processor;
        KeyedFactory<?, ?> factory;
        try {
            processor = (KeyedConfigProcessor<?>)processorMethod.invoke(null);
            if (processor == null) {
                throw new ComponentException(
                        "Processor method invocation returned null for " + component.getTypeName());
            }

            factory = (KeyedFactory<?, ?>)factoryMethod.invoke(null);
            if (factory == null) {
                throw new ComponentException("Factory method invocation returned null for " + component.getTypeName());
            }
        }
        catch (InvocationTargetException | IllegalAccessException e) {
            //rethrow as runtime exception, we already signature-checked the methods so this shouldn't generally happen
            throw new IllegalStateException(e);
        }

        @Subst(Namespaces.PHANTAZM + ":test")
        String value = annotation.value();

        Key componentKey;
        try {
            componentKey = Key.key(value);
        }
        catch (InvalidKeyException e) {
            throw new ComponentException("Invalid component name for " + component.getTypeName(), e);
        }

        configRegistry.registerProcessor(componentKey, processor);
        factoryRegistry.registerFactory(componentKey, factory);
    }

    @Override
    public <TData extends Keyed, TComponent> TComponent makeComponent(@NotNull ConfigNode node,
                                                                      @NotNull DependencyProvider provider)
            throws ComponentException {
        Keyed data;
        try {
            data = configRegistry.deserialize(node);
        }
        catch (ConfigProcessException e) {
            throw new ComponentException(e);
        }

        Key dataKey = data.key();
        KeyedFactory<TData, ?> factory = factoryRegistry.getFactory(dataKey);

        if (!provider.load(factory.dependencies())) {
            throw new ComponentException("Unable to prepare dependencies for data " + dataKey);
        }

        //noinspection unchecked
        return (TComponent)factory.make(provider, (TData)data);
    }
}
