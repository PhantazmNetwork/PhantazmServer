package org.phantazm.zombies.equipment.gun.target.entityfinder.directional;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Finds entities given the start and end of a shot.
 */
@FunctionalInterface
public interface DirectionalEntityFinder {

    /**
     * Finds entities given the start and end of a shot.
     *
     * @param instance The instance to search in
     * @param start    The start of the shot
     * @param end      The end of the shot
     */
    void findEntities(@NotNull Instance instance, @NotNull Pos start, @NotNull Point end,
        @NotNull Consumer<? super LivingEntity> callback);

}
