package com.github.phantazmnetwork.commons.component;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.component.annotation.*;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BasicComponentBuilderTest {
    @Test
    void throwsWhenNoComponentModel() {
        ComponentBuilder builder = new BasicComponentBuilder(Mockito.mock(KeyedConfigRegistry.class),
                                                             Mockito.mock(KeyedFactoryRegistry.class)
        );
        assertThrows(ComponentException.class, () -> builder.registerComponentClass(NoComponentModel.class));
    }

    @Test
    void throwsWhenNoFactory() {
        ComponentBuilder builder = new BasicComponentBuilder(Mockito.mock(KeyedConfigRegistry.class),
                                                             Mockito.mock(KeyedFactoryRegistry.class)
        );
        assertThrows(ComponentException.class, () -> builder.registerComponentClass(NoFactoryModel.class));
    }

    @Test
    void throwsWhenNoProcessor() {
        ComponentBuilder builder = new BasicComponentBuilder(Mockito.mock(KeyedConfigRegistry.class),
                                                             Mockito.mock(KeyedFactoryRegistry.class)
        );
        assertThrows(ComponentException.class, () -> builder.registerComponentClass(NoProcessorModel.class));
    }

    @Test
    void basicComponent() throws ComponentException {
        ComponentBuilder builder =
                new BasicComponentBuilder(new BasicKeyedConfigRegistry(), new BasicKeyedFactoryRegistry());
        builder.registerComponentClass(TestComponent.class);

        ConfigNode componentData = new LinkedConfigNode(2);
        componentData.putString("serialKey", "phantazm:test");
        componentData.putNumber("number", 69);
        componentData.putString("string", "vegetals");

        Map<Key, Object> dependencyMap = new HashMap<>(1);
        dependencyMap.put(TestComponent.FACTORY.dependencies().get(0), 420);

        DependencyProvider provider = DependencyProvider.lazy(dependencyMap::get);

        TestComponent component = builder.makeComponent(componentData, provider);
        TestComponent.Data data = component.data;

        assertEquals(69, data.number);
        assertEquals("vegetals", data.string);
        assertEquals(component.dependency, 420);
    }

    @Test
    void justDataInferredFactory() {
        ComponentBuilder builder =
                new BasicComponentBuilder(new BasicKeyedConfigRegistry(), new BasicKeyedFactoryRegistry());
        assertDoesNotThrow(() -> builder.registerComponentClass(JustDataInferredFactoryModel.class));
    }

    @Test
    void explicitDependencyConstructorFactory() throws ComponentException {
        ComponentBuilder builder =
                new BasicComponentBuilder(new BasicKeyedConfigRegistry(), new BasicKeyedFactoryRegistry());
        assertDoesNotThrow(() -> builder.registerComponentClass(ExplicitDependencyConstructorFactory.class));

        ConfigNode node = new LinkedConfigNode(1);
        node.putString("serialKey", "phantazm:test");

        Map<Key, Object> depMap = new HashMap<>();
        depMap.put(Key.key("phantazm:test.value"), 69420);

        DependencyProvider deps = new LazyDependencyProvider(depMap::get);

        ExplicitDependencyConstructorFactory component = builder.makeComponent(node, deps);
        assertEquals(69420, component.value);
    }

    @Test
    void implicitDependencyConstructorFactory() throws ComponentException {
        ComponentBuilder builder =
                new BasicComponentBuilder(new BasicKeyedConfigRegistry(), new BasicKeyedFactoryRegistry());
        assertDoesNotThrow(() -> builder.registerComponentClass(ImplicitDependencyConstructorFactory.class));

        ConfigNode node = new LinkedConfigNode(1);
        node.putString("serialKey", "phantazm:test");

        Map<Key, Object> depMap = new HashMap<>();
        depMap.put(Key.key("phantazm:test.value"), new ImplicitDependencyConstructorFactory.Dependency(42069));

        DependencyProvider deps = new LazyDependencyProvider(depMap::get);

        ImplicitDependencyConstructorFactory component = builder.makeComponent(node, deps);
        assertEquals(42069, component.dependency.value);
    }

    @Test
    void noDependenciesOrData() throws ComponentException {
        ComponentBuilder builder =
                new BasicComponentBuilder(new BasicKeyedConfigRegistry(), new BasicKeyedFactoryRegistry());
        assertDoesNotThrow(() -> builder.registerComponentClass(NoDependenciesOrData.class));

        ConfigNode node = new LinkedConfigNode(1);
        node.putString("serialKey", "phantazm:test");

        NoDependenciesOrData comp = builder.makeComponent(node, DependencyProvider.EMPTY);
        assertEquals("test_string", comp.internal);
    }

    @Test
    void justDependencies() throws ComponentException {
        ComponentBuilder builder =
                new BasicComponentBuilder(new BasicKeyedConfigRegistry(), new BasicKeyedFactoryRegistry());
        assertDoesNotThrow(() -> builder.registerComponentClass(JustDependencies.class));

        ConfigNode node = new LinkedConfigNode(1);
        node.putString("serialKey", "phantazm:test");

        Map<Key, Object> depMap = new HashMap<>(2);
        depMap.put(Key.key("phantazm:dependency1"), 42069);
        depMap.put(Key.key("phantazm:dependency2"), "test");

        DependencyProvider deps = new LazyDependencyProvider(depMap::get);
        JustDependencies component = builder.makeComponent(node, deps);

        assertEquals(42069, component.dependency1);
        assertEquals("test", component.dependency2);
    }

    @Test
    void justData() throws ComponentException {
        ComponentBuilder builder =
                new BasicComponentBuilder(new BasicKeyedConfigRegistry(), new BasicKeyedFactoryRegistry());
        assertDoesNotThrow(() -> builder.registerComponentClass(JustData.class));

        ConfigNode node = new LinkedConfigNode(2);
        node.putString("serialKey", "phantazm:test");
        node.putNumber("data", 69);

        JustData data = builder.makeComponent(node, DependencyProvider.EMPTY);

        assertEquals(new JustData.Data(69), data.data);
    }

    @ComponentModel("phantazm:test")
    public static class JustData {
        @ComponentProcessor
        public static KeyedConfigProcessor<?> processor() {
            return new KeyedConfigProcessor<Data>() {
                @Override
                public @NotNull Data dataFromNode(@NotNull ConfigNode node) throws ConfigProcessException {
                    return new Data(node.getNumberOrThrow("data").intValue());
                }

                @Override
                public @NotNull ConfigNode nodeFromData(@NotNull Data data) {
                    ConfigNode node = new LinkedConfigNode(1);
                    node.putNumber("data", data.data);
                    return node;
                }
            };
        }

        @ComponentData
        public record Data(int data) implements Keyed {

            @Override
            public @NotNull Key key() {
                return Key.key("phantazm:just_data");
            }
        }

        private final Data data;

        @ComponentFactory
        public JustData(Data data) {
            this.data = data;
        }
    }

    @ComponentModel("phantazm:test")
    public static class JustDependencies {
        private final int dependency1;
        private final String dependency2;

        @ComponentFactory
        public JustDependencies(@ComponentDependency("phantazm:dependency1") int dependency1,
                                @ComponentDependency("phantazm:dependency2") String dependency2) {
            this.dependency1 = dependency1;
            this.dependency2 = dependency2;
        }
    }

    @ComponentModel("phantazm:test")
    public static class NoDependenciesOrData {
        private final String internal;

        @ComponentFactory
        public NoDependenciesOrData() {
            this.internal = "test_string";
        }
    }

    @ComponentModel("phantazm:test")
    public static class ImplicitDependencyConstructorFactory {
        @ComponentDependency("phantazm:test.value")
        public record Dependency(int value) {

        }

        @ComponentProcessor
        public static KeyedConfigProcessor<?> processor() {
            return KeyedConfigProcessor.fromSupplier(Data::new);
        }

        private final Dependency dependency;

        @ComponentFactory
        public ImplicitDependencyConstructorFactory(@NotNull Data data, @NotNull Dependency dependency) {
            this.dependency = dependency;
        }

        @ComponentData
        private record Data() implements Keyed {
            @Override
            public @NotNull Key key() {
                return Key.key("phantazm:test");
            }
        }
    }

    @ComponentModel("phantazm:test")
    public static class ExplicitDependencyConstructorFactory {
        @ComponentProcessor
        public static KeyedConfigProcessor<?> processor() {
            return KeyedConfigProcessor.fromSupplier(Data::new);
        }

        private final int value;

        @ComponentFactory
        public ExplicitDependencyConstructorFactory(@ComponentData @NotNull Data data,
                                                    @ComponentDependency("phantazm:test.value") int value) {
            this.value = value;
        }

        private record Data() implements Keyed {
            @Override
            public @NotNull Key key() {
                return Key.key("phantazm:test");
            }
        }
    }

    @ComponentModel("phantazm:test")
    public static class JustDataInferredFactoryModel {
        @ComponentProcessor
        public static KeyedConfigProcessor<?> processor() {
            return KeyedConfigProcessor.fromSupplier(Data::new);
        }

        @ComponentFactory
        public JustDataInferredFactoryModel(@ComponentData @NotNull Data data) {

        }

        private record Data() implements Keyed {

            @Override
            public @NotNull Key key() {
                throw new UnsupportedOperationException();
            }
        }
    }

    public static class NoComponentModel {
        @ComponentFactory
        public static KeyedFactory<?, ?> factory() {
            return null;
        }

        @ComponentProcessor
        public static KeyedConfigProcessor<?> processor() {
            return null;
        }
    }

    @ComponentModel("phantazm:test")
    public static class NoFactoryModel {
        public static KeyedFactory<?, ?> factory() {
            return null;
        }

        @ComponentProcessor
        public static KeyedConfigProcessor<?> processor() {
            return null;
        }
    }

    @ComponentModel("phantazm:test")
    public static class NoProcessorModel {
        @ComponentFactory
        public static KeyedFactory<?, ?> factory() {
            return null;
        }

        public static KeyedConfigProcessor<?> processor() {
            return null;
        }
    }

    @ComponentModel("phantazm:test")
    public static class TestComponent {
        private static final KeyedConfigProcessor<Data> PROCESSOR = new KeyedConfigProcessor<>() {
            @Override
            public @NotNull Data dataFromNode(@NotNull ConfigNode node) throws ConfigProcessException {
                int number = node.getNumberOrThrow("number").intValue();
                String string = node.getStringOrThrow("string");
                return new Data(number, string);
            }

            @Override
            public @NotNull ConfigNode nodeFromData(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(2);
                node.putNumber("number", data.number);
                node.putString("string", data.string);
                return node;
            }
        };
        private static final KeyedFactory<Data, TestComponent> FACTORY = new KeyedFactory<>() {
            private static final List<Key> DEPENDENCIES =
                    List.of(Key.key(Namespaces.PHANTAZM, "test.dependency.number"));

            @Override
            public @NotNull TestComponent make(@NotNull DependencyProvider dependencyProvider, Data data) {
                int dependency = dependencyProvider.provide(DEPENDENCIES.get(0));
                return new TestComponent(data, dependency);
            }

            @Override
            public @Unmodifiable @NotNull List<Key> dependencies() {
                return DEPENDENCIES;
            }
        };
        private final Data data;
        private final int dependency;

        public TestComponent(Data data, int dependency) {
            this.data = data;
            this.dependency = dependency;
        }

        @ComponentFactory
        public static KeyedFactory<Data, TestComponent> factory() {
            return FACTORY;
        }

        @ComponentProcessor
        public static KeyedConfigProcessor<Data> processor() {
            return PROCESSOR;
        }

        public record Data(int number, String string) implements Keyed {
            public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "test");

            @Override
            public @NotNull Key key() {
                return SERIAL_KEY;
            }
        }
    }
}
