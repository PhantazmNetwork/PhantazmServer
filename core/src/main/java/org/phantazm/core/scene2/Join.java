package org.phantazm.core.scene2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.player.PlayerView;

import java.util.Set;

public interface Join<T extends Scene> {
    @NotNull @Unmodifiable Set<@NotNull PlayerView> players();

    @NotNull Class<T> targetType();

    @NotNull T createNewScene();

    boolean canCreateNewScene();


    /**
     * Joins the given scene. {@code scene} <i>must</i> be acquired when this method is called, so it is safe to use any
     * method on the object. This method <i>may</i> throw an unchecked exception when called for any scene {@code scene}
     * such that {@code Join#matches(scene)} returns {@code false}.
     * <p>
     * This method is expected to perform actions such as teleporting players to a new instance, sending tablist
     * packets, and other operations, as appropriate.
     *
     * @param scene the scene to join
     */
    void join(@NotNull T scene);

    boolean matches(@NotNull T scene);
}
