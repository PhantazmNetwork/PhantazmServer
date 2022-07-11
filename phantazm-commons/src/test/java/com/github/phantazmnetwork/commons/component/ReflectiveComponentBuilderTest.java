package com.github.phantazmnetwork.commons.component;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.component.annotation.ComponentFactory;
import com.github.phantazmnetwork.commons.component.annotation.ComponentModel;
import com.github.phantazmnetwork.commons.component.annotation.ComponentProcessor;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReflectiveComponentBuilderTest {
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

    @ComponentModel
    public static class NoFactoryModel {
        public static KeyedFactory<?, ?> factory() {
            return null;
        }

        @ComponentProcessor
        public static KeyedConfigProcessor<?> processor() {
            return null;
        }
    }

    @ComponentModel
    public static class NoProcessorModel {
        @ComponentFactory
        public static KeyedFactory<?, ?> factory() {
            return null;
        }

        public static KeyedConfigProcessor<?> processor() {
            return null;
        }
    }

    @ComponentModel
    public static class TestComponent {
        public record Data(int number, String string) implements Keyed {
            public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "test");

            @Override
            public @NotNull Key key() {
                return SERIAL_KEY;
            }
        }

        private static final KeyedConfigProcessor<Data> PROCESSOR = new KeyedConfigProcessor<>() {
            @Override
            public @NotNull Key key() {
                return Data.SERIAL_KEY;
            }

            @Override
            public Data dataFromNode(@NotNull ConfigNode node)
                    throws ConfigProcessException {
                int number = node.getNumberOrThrow("number").intValue();
                String string = node.getStringOrThrow("string");
                return new Data(number, string);
            }

            @Override
            public @NotNull ConfigNode nodeFromData(Data data) {
                ConfigNode node = new LinkedConfigNode(2);
                node.putNumber("number", data.number);
                node.putString("string", data.string);
                return node;
            }
        };

        private static final KeyedFactory<Data, TestComponent> FACTORY = new KeyedFactory<>() {
            private static final List<Key> DEPENDENCIES = List.of(Key.key(Namespaces.PHANTAZM,
                    "test.dependency.number"));

            @Override
            public @NotNull TestComponent make(@NotNull DependencyProvider dependencyProvider, @NotNull Data data) {
                int dependency = dependencyProvider.provide(DEPENDENCIES.get(0));
                return new TestComponent(data, dependency);
            }

            @Override
            public @Unmodifiable @NotNull List<Key> dependencySpec() {
                return DEPENDENCIES;
            }

            @Override
            public @NotNull Key key() {
                return Data.SERIAL_KEY;
            }
        };

        private final Data data;
        private final int dependency;

        public TestComponent(Data data, int dependency) {
            this.data = data;
            this.dependency = dependency;
        }

        @ComponentFactory
        public static KeyedFactory<?, ?> factory() {
            return FACTORY;
        }

        @ComponentProcessor
        public static KeyedConfigProcessor<Data> processor() {
            return PROCESSOR;
        }
    }

    @Test
    void throwsWhenNoComponentModel() {
        ComponentBuilder builder = new ReflectiveComponentBuilder(Mockito.mock(KeyedConfigRegistry.class), Mockito
                .mock(KeyedFactoryRegistry.class));
        assertThrows(IllegalArgumentException.class, () -> builder.registerComponentClass(NoComponentModel.class));
    }

    @Test
    void throwsWhenNoFactory() {
        ComponentBuilder builder = new ReflectiveComponentBuilder(Mockito.mock(KeyedConfigRegistry.class), Mockito
                .mock(KeyedFactoryRegistry.class));
        assertThrows(IllegalArgumentException.class, () -> builder.registerComponentClass(NoFactoryModel.class));
    }

    @Test
    void throwsWhenNoProcessor() {
        ComponentBuilder builder = new ReflectiveComponentBuilder(Mockito.mock(KeyedConfigRegistry.class), Mockito
                .mock(KeyedFactoryRegistry.class));
        assertThrows(IllegalArgumentException.class, () -> builder.registerComponentClass(NoProcessorModel.class));
    }

    @Test
    void basicComponent() {
        ComponentBuilder builder = new ReflectiveComponentBuilder(new BasicKeyedConfigRegistry(),
                new BasicKeyedFactoryRegistry());
        builder.registerComponentClass(TestComponent.class);

        ConfigNode componentData = new LinkedConfigNode(2);
        componentData.putString("serialKey", "phantazm:test");
        componentData.putNumber("number", 69);
        componentData.putString("string", "vegetals");

        Map<Key, Object> dependencyMap = new HashMap<>(1);
        dependencyMap.put(TestComponent.FACTORY.dependencySpec().get(0), 420);

        DependencyProvider provider = DependencyProvider.lazy(dependencyMap::get);

        TestComponent component = builder.makeComponent(componentData, provider);
        TestComponent.Data data = component.data;
        assertEquals(69, data.number);
        assertEquals("vegetals", data.string);

        assertEquals(component.dependency, 420);
    }
}