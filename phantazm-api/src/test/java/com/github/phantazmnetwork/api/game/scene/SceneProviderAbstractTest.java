package com.github.phantazmnetwork.api.game.scene;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class SceneProviderAbstractTest {

    private final int maximumLobbies = 10;

    @Test
    public void testMaximumLobbies() {
        SceneProvider<Scene<SceneJoinRequest>, SceneJoinRequest> sceneProvider = new SceneProviderAbstract<>(maximumLobbies) {
            @Override
            protected @NotNull Optional<Scene<SceneJoinRequest>> chooseScene(@NotNull SceneJoinRequest o) {
                return Optional.empty();
            }

            @SuppressWarnings("unchecked")
            @Override
            protected @NotNull Scene<SceneJoinRequest> createScene(@NotNull SceneJoinRequest o) {
                return (Scene<SceneJoinRequest>) mock(Scene.class);
            }
        };

        SceneJoinRequest request = Mockito.mock(SceneJoinRequest.class);
        for (int i = 0; i < maximumLobbies; i++) {
            assertTrue(sceneProvider.provideScene(request).isPresent());
        }
        assertTrue(sceneProvider.provideScene(request).isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProviderScenesAreTicked() {
        Collection<Scene<SceneJoinRequest>> scenes = new ArrayList<>(maximumLobbies);
        for (int i = 0; i < maximumLobbies; i++) {
            scenes.add(mock(Scene.class));
        }

        SceneProvider<Scene<SceneJoinRequest>, SceneJoinRequest> sceneProvider = new SceneProviderAbstract<>(maximumLobbies) {

            private final Iterator<Scene<SceneJoinRequest>> iterator = scenes.iterator();

            @Override
            protected @NotNull Optional<Scene<SceneJoinRequest>> chooseScene(@NotNull SceneJoinRequest o) {
                return Optional.empty();
            }

            @Override
            protected @NotNull Scene<SceneJoinRequest> createScene(@NotNull SceneJoinRequest o) {
                return iterator.next();
            }

        };

        SceneJoinRequest request = Mockito.mock(SceneJoinRequest.class);
        for (int i = 0; i < maximumLobbies; i++) {
            sceneProvider.provideScene(request);
        }
        sceneProvider.tick(0);

        for (Scene<SceneJoinRequest> scene : scenes) {
            Mockito.verify(scene).tick(0);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProviderRemovesShutdownScenes() {
        Scene<SceneJoinRequest> scene = (Scene<SceneJoinRequest>) mock(Scene.class);
        when(scene.isShutdown()).thenReturn(true);

        SceneProvider<Scene<SceneJoinRequest>, SceneJoinRequest> sceneProvider = new SceneProviderAbstract<>(maximumLobbies) {
            @Override
            protected @NotNull Optional<Scene<SceneJoinRequest>> chooseScene(@NotNull SceneJoinRequest o) {
                return Optional.empty();
            }

            @Override
            protected @NotNull Scene<SceneJoinRequest> createScene(@NotNull SceneJoinRequest o) {
                return scene;
            }
        };

        SceneJoinRequest request = Mockito.mock(SceneJoinRequest.class);
        sceneProvider.provideScene(request);
        sceneProvider.tick(0);

        assertFalse(sceneProvider.getScenes().iterator().hasNext());
        verify(scene, never()).tick(0);
    }

}
