package org.phantazm.core.scene2;

/**
 * A {@link Scene} whose joinable state can be changed by calling a method. However, the scene will still remain
 * non-joinable during shutdown.
 */
public interface JoinToggleableScene extends Scene {
    /**
     * Sets the joinable status of this scene. Setting this to {@code true} will not work if the scene is non-joinable
     * due to having been shut down.
     *
     * @param joinable {@code true} if the scene should be joinable; {@code false} otherwise
     */
    void setJoinable(boolean joinable);
}
