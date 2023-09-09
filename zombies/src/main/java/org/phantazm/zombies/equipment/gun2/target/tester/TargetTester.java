package org.phantazm.zombies.equipment.gun2.target.tester;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

@FunctionalInterface
public interface TargetTester {

    /**
     * Test whether an {@link Entity} target should be considered as an actual target.
     *
     * @param target       The {@link Entity} target to test
     * @param previousHits A {@link Collection} of previously hit {@link UUID}s
     * @return Whether the {@link Entity} target should be considered as an actual target
     */
    boolean useTarget(@NotNull Entity target, @NotNull Collection<UUID> previousHits);

}
