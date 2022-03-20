package com.github.phantazmnetwork.api.game.scene;

import com.github.phantazmnetwork.api.util.Tickable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Optional;

/**
 * A provider to create new {@link Scene}s.
 * @param <TScene> The {@link Scene} that the provider creates
 */
public interface SceneProvider<TScene extends Scene<?>> extends Tickable {

    /**
     * Provides a {@link Scene}.
     * @return An {@link Optional} of a {@link Scene}. The scene may be newly created or from the provider's store.
     */
    @NotNull Optional<TScene> provideScene();

    /**
     * Gets the {@link Scene}s currently stored by the {@link Scene} provider.
     * @return A view of the {@link Scene}s
     */
    @UnmodifiableView @NotNull Iterable<TScene> getScenes();

    /**
     * Shuts down the scene provider.
     */
    void forceShutdown();

}
