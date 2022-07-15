package com.github.phantazmnetwork.api.game.scene;

import com.github.phantazmnetwork.commons.Tickable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Optional;

/**
 * A provider to create new {@link Scene}s.
 *
 * @param <TRequest>> The requests used for the {@link Scene}s that the provider creates
 */
public interface SceneProvider<TScene extends Scene<TRequest>, TRequest extends SceneJoinRequest> extends Tickable {

    /**
     * Provides a {@link Scene}.
     *
     * @param request The request used to provide an appropriate scene
     * @return An {@link Optional} of a {@link Scene}. The scene may be newly created or from the provider's store.
     */
    @NotNull Optional<TScene> provideScene(@NotNull TRequest request);

    /**
     * Gets the {@link Scene}s currently stored by the {@link Scene} provider.
     *
     * @return A view of the {@link Scene}s
     */
    @UnmodifiableView @NotNull Iterable<TScene> getScenes();

    /**
     * Shuts down the scene provider.
     */
    void forceShutdown();

}
