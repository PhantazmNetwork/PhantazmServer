package org.phantazm.core.game.scene;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class SceneProviderAbstractTest {

    private final int maximumLobbies = 10;

    private ExecutorService executor;

    @BeforeEach
    public void setup() {
        executor = Executors.newSingleThreadExecutor();
    }

    @Test
    public void testMaximumLobbies() {
        SceneProvider<Scene<SceneJoinRequest>, SceneJoinRequest> sceneProvider =
                new SceneProviderAbstract<>(executor,maximumLobbies) {
                    @Override
                    protected @NotNull Optional<TransferResult> chooseScene(@NotNull SceneJoinRequest o) {
                        return Optional.empty();
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    protected @NotNull CompletableFuture<Scene<SceneJoinRequest>> createScene(@NotNull SceneJoinRequest o) {
                        Scene<SceneJoinRequest> scene = mock(Scene.class);
                        when(scene.join(ArgumentMatchers.any())).thenReturn(TransferResult.failure(Component.empty()));
                        return CompletableFuture.completedFuture(scene);
                    }

                    @Override
                    protected void cleanupScene(@NotNull Scene<SceneJoinRequest> scene) {

                    }
                };

        SceneJoinRequest request = Mockito.mock(SceneJoinRequest.class);
        for (int i = 0; i < maximumLobbies; i++) {
            assertTrue(sceneProvider.provideScene(request).join().isPresent());
        }
        assertTrue(sceneProvider.provideScene(request).join().isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProviderScenesAreTicked() {
        Collection<Scene<SceneJoinRequest>> scenes = new ArrayList<>(maximumLobbies);
        for (int i = 0; i < maximumLobbies; i++) {
            Scene<SceneJoinRequest> scene = mock(Scene.class);
            when(scene.join(ArgumentMatchers.any())).thenReturn(TransferResult.failure(Component.empty()));
            scenes.add(scene);
        }

        SceneProvider<Scene<SceneJoinRequest>, SceneJoinRequest> sceneProvider =
                new SceneProviderAbstract<>(executor,maximumLobbies) {

                    private final Iterator<Scene<SceneJoinRequest>> iterator = scenes.iterator();

                    @Override
                    protected @NotNull Optional<TransferResult> chooseScene(@NotNull SceneJoinRequest o) {
                        return Optional.empty();
                    }

                    @Override
                    protected @NotNull CompletableFuture<Scene<SceneJoinRequest>> createScene(@NotNull SceneJoinRequest o) {
                        return CompletableFuture.completedFuture(iterator.next());
                    }

                    @Override
                    protected void cleanupScene(@NotNull Scene<SceneJoinRequest> scene) {

                    }

                };

        SceneJoinRequest request = Mockito.mock(SceneJoinRequest.class);
        for (int i = 0; i < maximumLobbies; i++) {
            sceneProvider.provideScene(request).join();
        }
        sceneProvider.tick(0);

        for (Scene<SceneJoinRequest> scene : scenes) {
            Mockito.verify(scene).tick(0);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProviderRemovesShutdownScenes() {
        Scene<SceneJoinRequest> scene = mock(Scene.class);
        when(scene.join(ArgumentMatchers.any())).thenReturn(TransferResult.failure(Component.empty()));
        when(scene.isShutdown()).thenReturn(true);

        SceneProvider<Scene<SceneJoinRequest>, SceneJoinRequest> sceneProvider =
                new SceneProviderAbstract<>(executor, maximumLobbies) {
                    @Override
                    protected @NotNull Optional<TransferResult> chooseScene(@NotNull SceneJoinRequest o) {
                        return Optional.empty();
                    }

                    @Override
                    protected @NotNull CompletableFuture<Scene<SceneJoinRequest>> createScene(@NotNull SceneJoinRequest o) {
                        return CompletableFuture.completedFuture(scene);
                    }

                    @Override
                    protected void cleanupScene(@NotNull Scene<SceneJoinRequest> scene) {

                    }
                };

        SceneJoinRequest request = Mockito.mock(SceneJoinRequest.class);
        sceneProvider.provideScene(request).join();
        sceneProvider.tick(0);

        assertFalse(sceneProvider.getScenes().iterator().hasNext());
        verify(scene, never()).tick(0);
    }

    @AfterEach
    public void tearDown() {
        executor.shutdownNow();
    }

}
