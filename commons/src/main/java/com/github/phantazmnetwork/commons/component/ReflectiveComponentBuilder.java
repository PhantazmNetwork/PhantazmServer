package com.github.phantazmnetwork.commons.component;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.ReflectionUtils;
import com.github.phantazmnetwork.commons.component.annotation.ComponentDependency;
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
        if (configRegistry.hasProcessor(componentKey)) {
            throw new ComponentException("Component already registered under " + componentKey);
        }

        if (factoryRegistry.hasFactory(componentKey)) {
            throw new ComponentException("Factory already registered under " + componentKey);
        }

        KeyedConfigProcessor<?> processor = getProcessor(component);
        KeyedFactory<?, ?> factory = getFactory(component);

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
            throw new ComponentException("No data processor method found in " + component.getTypeName());
        }

        return tryInvokeMethod(declaredProcessor);
    }

    private static KeyedFactory<?, ?> getFactory(Class<?> component) throws ComponentException {
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
            throw new ComponentException("Zero-length factory constructor found in " + component.getTypeName());
        }

        Parameter data = parameters[0];
        Class<?> dataClass = data.getType();
        if (!Keyed.class.isAssignableFrom(dataClass)) {
            throw new ComponentException(
                    "Found data argument that did not subclass Keyed in " + component.getTypeName());
        }

        if (parameters.length == 1) {
            return new KeyedFactory<>() {
                @Override
                public @NotNull Object make(@NotNull DependencyProvider dependencyProvider, @NotNull Keyed keyed) {
                    return tryInvokeConstructor(declaredConstructor, keyed);
                }

                @Override
                public @Unmodifiable @NotNull List<Key> dependencies() {
                    return Collections.emptyList();
                }
            };
        }

        List<Key> dependencyKeys = new ArrayList<>(parameters.length - 1);
        List<Key> unmodifiableView = Collections.unmodifiableList(dependencyKeys);
        for (int i = 1; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            ComponentDependency dependency = parameter.getAnnotation(ComponentDependency.class);
            if (dependency == null) {
                dependency = parameter.getType().getAnnotation(ComponentDependency.class);

                //look on the dependency type as a fallback to find out its key
                if (dependency == null) {
                    throw new ComponentException(
                            "Found component dependency without the ComponentDependency annotation in " +
                            component.getTypeName());
                }
            }

            Key dependencyKey = getKey(component, dependency.value());
            dependencyKeys.add(dependencyKey);
        }

        return new KeyedFactory<>() {
            @Override
            public @NotNull Object make(@NotNull DependencyProvider dependencyProvider, @NotNull Keyed keyed) {
                Object[] args = new Object[dependencyKeys.size() + 1];
                args[0] = keyed;
                for (int i = 1; i < args.length; i++) {
                    args[i] = dependencyProvider.provide(dependencyKeys.get(i - 1));
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
