package org.phantazm.core.scene2;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * A scene that provides a unique identifier. By convention, this identifier should not be <i>equal</i> to the
 * identifier of any other {@link IdentifiableScene}.
 */
public interface IdentifiableScene extends Scene {
    /**
     * The unique identifier of this scene. This must not change for the usable lifetime of the scene; therefore, it
     * should be threadsafe.
     *
     * @return this scene's identifier
     */
    @NotNull UUID identity();
}
