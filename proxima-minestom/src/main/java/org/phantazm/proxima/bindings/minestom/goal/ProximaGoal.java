package org.phantazm.proxima.bindings.minestom.goal;

import org.phantazm.commons.Activable;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;

/**
 * Represents a goal for a {@link ProximaEntity} that usually controls its behavior.
 */
public interface ProximaGoal extends Activable {

    /**
     * Gets whether the goal should start.
     *
     * @return Whether the goal should start
     */
    boolean shouldStart();

    /**
     * Gets whether the goal should end.
     *
     * @return Whether the goal should end
     */
    boolean shouldEnd();

}
