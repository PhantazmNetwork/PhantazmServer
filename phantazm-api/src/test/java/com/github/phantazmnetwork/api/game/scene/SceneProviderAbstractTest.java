package com.github.phantazmnetwork.api.game.scene;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SceneProviderAbstractTest {

    private final int maximumLobbies = 10;

    @Test
    public void testMaximumLobbies() {
        SceneProvider<Scene<Object>, Object> sceneProvider = new SceneProviderAbstract<>(maximumLobbies) {
            @Override
            protected @NotNull Optional<Scene<Object>> chooseScene(@NotNull Object o) {
                return Optional.empty();
            }

            @SuppressWarnings("unchecked")
            @Override
            protected @NotNull Scene<Object> createScene(@NotNull Object o) {
                return (Scene<Object>) mock(Scene.class);
            }
        };

        Object request = new Object();
        for (int i = 0; i < maximumLobbies; i++) {
            assertTrue(sceneProvider.provideScene(request).isPresent());
        }
        assertTrue(sceneProvider.provideScene(request).isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProviderScenesAreTicked() {
        Collection<Scene<Object>> scenes = new ArrayList<>(maximumLobbies);
        for (int i = 0; i < maximumLobbies; i++) {
            scenes.add(mock(Scene.class));
        }

        SceneProvider<Scene<Object>, Object> sceneProvider = new SceneProviderAbstract<>(maximumLobbies) {

            private final Iterator<Scene<Object>> iterator = scenes.iterator();

            @Override
            protected @NotNull Optional<Scene<Object>> chooseScene(@NotNull Object o) {
                return Optional.empty();
            }

            @Override
            protected @NotNull Scene<Object> createScene(@NotNull Object o) {
                return iterator.next();
            }

        };

        Object request = new Object();
        for (int i = 0; i < maximumLobbies; i++) {
            sceneProvider.provideScene(request);
        }
        sceneProvider.tick();

        for (Scene<Object> scene : scenes) {
            Mockito.verify(scene).tick();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProviderRemovesShutdownScenes() {
        Scene<Object> scene = (Scene<Object>) mock(Scene.class);
        when(scene.isShutdown()).thenReturn(true);

        SceneProvider<Scene<Object>, Object> sceneProvider = new SceneProviderAbstract<>(maximumLobbies) {
            @Override
            protected @NotNull Optional<Scene<Object>> chooseScene(@NotNull Object o) {
                return Optional.empty();
            }

            @Override
            protected @NotNull Scene<Object> createScene(@NotNull Object o) {
                return scene;
            }
        };

        Object request = new Object();
        sceneProvider.provideScene(request);
        sceneProvider.tick();

        assertFalse(sceneProvider.getScenes().iterator().hasNext());
        verify(scene, never()).tick();
    }

}
