package com.github.phantazmnetwork.commons.component;

import com.github.phantazmnetwork.commons.component.annotation.ComponentFactory;
import com.github.phantazmnetwork.commons.component.annotation.ComponentModel;
import com.github.phantazmnetwork.commons.component.annotation.ComponentProcessor;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class ReflectiveComponentBuilderTest {
    public static class NoComponentModel {
        @ComponentFactory
        public static KeyedFactory<?, ?> factory() {
            return null;
        }

        @ComponentProcessor
        public static ConfigProcessor<?> processor() {
            return null;
        }
    }

    @ComponentModel
    public static class NoFactoryModel {
        public static KeyedFactory<?, ?> factory() {
            return null;
        }

        @ComponentProcessor
        public static ConfigProcessor<?> processor() {
            return null;
        }
    }

    @ComponentModel
    public static class NoProcessorModel {
        @ComponentFactory
        public static KeyedFactory<?, ?> factory() {
            return null;
        }

        public static ConfigProcessor<?> processor() {
            return null;
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
}