package com.github.phantazmnetwork.commons.component;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.ReflectionUtils;
import com.github.phantazmnetwork.commons.component.annotation.*;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Standard reflection-based implementation of {@link ComponentBuilder}. This is the main entrypoint of the component
 * framework.
 */
public class ReflectiveComponentBuilder implements ComponentBuilder {
    private static final Predicate<Method> BASE_METHOD = method -> {
        int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && method.getParameterCount() == 0;
    };

    private static final Predicate<Constructor<?>> BASE_CONSTRUCTOR =
            constructor -> Modifier.isPublic(constructor.getModifiers());

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

        String value = annotation.value();

        Key componentKey = getKey(component, value);
        boolean hasProcessor = true;
        if (!configRegistry.hasProcessor(componentKey)) {
            KeyedConfigProcessor<?> processor = getProcessor(component);

            if (processor == null) {
                hasProcessor = false;
            }
            else {
                configRegistry.registerProcessor(componentKey, processor);
            }
        }

        if (!factoryRegistry.hasFactory(componentKey)) {
            KeyedFactory<?, ?> factory = getFactory(component, hasProcessor);
            factoryRegistry.registerFactory(componentKey, factory);
        }
    }

    @Override
    public <TData extends Keyed, TComponent> TComponent makeComponent(@NotNull ConfigNode node,
                                                                      @NotNull DependencyProvider provider)
            throws ComponentException {
        Key dataKey;
        Keyed data = null;
        try {
            dataKey = configRegistry.extractKey(node);

            if (configRegistry.hasProcessor(dataKey)) {
                data = configRegistry.deserialize(node);
            }
        }
        catch (ConfigProcessException e) {
            throw new ComponentException(e);
        }

        KeyedFactory<TData, ?> factory = factoryRegistry.getFactory(dataKey);
        if (factory == null) {
            throw new ComponentException("Unable to find a suitable factory for " + dataKey);
        }

        if (!provider.load(factory.dependencies())) {
            throw new ComponentException("Unable to prepare dependencies for data " + dataKey);
        }

        //noinspection unchecked
        return (TComponent)factory.make(provider, (TData)data);
    }

    private static Key getKey(Class<?> component, @Subst(Namespaces.PHANTAZM + ":test") String value)
            throws ComponentException {
        try {
            return Key.key(value);
        }
        catch (InvalidKeyException e) {
            throw new ComponentException("Invalid component name for " + component.getTypeName(), e);
        }
    }

    private static KeyedConfigProcessor<?> getProcessor(Class<?> component) throws ComponentException {
        Method declaredProcessor = ReflectionUtils.declaredMethodMatching(component, BASE_METHOD.and(
                method -> method.isAnnotationPresent(ComponentProcessor.class) &&
                          KeyedConfigProcessor.class.isAssignableFrom(method.getReturnType())));
        if (declaredProcessor == null) {
            return null;
        }

        return tryInvokeMethod(declaredProcessor);
    }

    private static KeyedFactory<?, ?> getFactory(Class<?> component, boolean hasProcessor) throws ComponentException {
        Method declaredFactory = ReflectionUtils.declaredMethodMatching(component, BASE_METHOD.and(
                method -> method.isAnnotationPresent(ComponentFactory.class) &&
                          KeyedFactory.class.isAssignableFrom(method.getReturnType())));
        if (declaredFactory != null) {
            //if present, use explicitly-declared static factory entrypoint method
            return tryInvokeMethod(declaredFactory);
        }

        //try to infer a reasonable factory from the constructor instead
        Constructor<?> declaredConstructor = ReflectionUtils.declaredConstructorMatching(component,
                                                                                         BASE_CONSTRUCTOR.and(
                                                                                                 constructor -> constructor.isAnnotationPresent(
                                                                                                         ComponentFactory.class))
        );
        if (declaredConstructor == null) {
            throw new ComponentException(
                    "No component factory method or constructor found in " + component.getTypeName());
        }

        Parameter[] parameters = declaredConstructor.getParameters();
        if (parameters.length == 0) {
            //zero-length constructor needs no data or dependencies
            return new KeyedFactory<>() {
                @Override
                public @NotNull Object make(@NotNull DependencyProvider dependencyProvider, Keyed keyed) {
                    return tryInvokeConstructor(declaredConstructor);
                }

                @Override
                public @Unmodifiable @NotNull List<Key> dependencies() {
                    return Collections.emptyList();
                }
            };
        }

        int dataParameterIndex = -1;
        List<Key> dependencyKeys = new ArrayList<>(parameters.length);
        List<Key> unmodifiableView = Collections.unmodifiableList(dependencyKeys);
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            //check for ComponentData
            if (parameter.isAnnotationPresent(ComponentData.class) ||
                parameter.getType().isAnnotationPresent(ComponentData.class)) {
                if (dataParameterIndex != -1) {
                    throw new ComponentException("Type " + component.getTypeName() + " has more than one " +
                                                 "ComponentData in its factory constructor");
                }
                dataParameterIndex = i;
            }
            else {
                ComponentDependency dependency = parameter.getAnnotation(ComponentDependency.class);
                if (dependency == null) {
                    dependency = parameter.getType().getAnnotation(ComponentDependency.class);
                    if (dependency == null) {
                        throw new ComponentException("Missing ComponentDependency annotation of parameter of factor " +
                                                     "constructor for type " + component.getTypeName());
                    }
                }

                dependencyKeys.add(getKey(component, dependency.value()));
            }
        }

        if (dataParameterIndex == -1 && hasProcessor) {
            throw new ComponentException(
                    "No data parameter found in component which specifies a processor " + component.getTypeName());
        }

        if (dataParameterIndex != -1 && !hasProcessor) {
            throw new ComponentException(
                    "Found data parameter in component which does not specify a processor " + component.getTypeName());
        }

        int finalDataParameterIndex = dataParameterIndex;

        return new KeyedFactory<>() {
            @Override
            public @NotNull Object make(@NotNull DependencyProvider dependencyProvider, Keyed keyed) {
                Object[] args;
                if (finalDataParameterIndex == -1) {
                    //no data parameter, object is entirely dependencies
                    args = new Object[dependencyKeys.size()];
                    for (int i = 0; i < dependencyKeys.size(); i++) {
                        args[i] = dependencyProvider.provide(dependencyKeys.get(i));
                    }
                }
                else {
                    args = new Object[dependencyKeys.size() + 1];
                    args[finalDataParameterIndex] = keyed;
                    for (int i = 0; i < finalDataParameterIndex; i++) {
                        args[i] = dependencyProvider.provide(dependencyKeys.get(i));
                    }

                    for (int i = finalDataParameterIndex + 1; i < parameters.length; i++) {
                        args[i] = dependencyProvider.provide(dependencyKeys.get(i - 1));
                    }
                }

                return tryInvokeConstructor(declaredConstructor, args);
            }

            @Override
            public @Unmodifiable @NotNull List<Key> dependencies() {
                return unmodifiableView;
            }
        };
    }

    private static <T> T tryInvokeMethod(Method method, Object... args) throws ComponentException {
        try {
            //noinspection unchecked
            T result = (T)method.invoke(null, args);
            if (result == null) {
                throw new ComponentException("Null result when invoking component processor entrypoint for " +
                                             method.getDeclaringClass().getTypeName());
            }
            return result;
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throw new ComponentException("Exception when invoking component processor entrypoint for " +
                                         method.getDeclaringClass().getTypeName(), e);
        }
    }

    private static Object tryInvokeConstructor(Constructor<?> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Reflective exception when attempting to instantiate component", e);
        }
    }
}
