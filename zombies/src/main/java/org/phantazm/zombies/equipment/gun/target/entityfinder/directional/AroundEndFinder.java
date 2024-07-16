package org.phantazm.zombies.equipment.gun.target.entityfinder.directional;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Finds entities around the end of a shot.
 */
@Model("zombies.gun.entity_finder.directional.around_end")
@Cache
public class AroundEndFinder implements DirectionalEntityFinder {
    private final Data data;

    /**
     * Creates a new {@link AroundEndFinder}.
     *
     * @param data The {@link Data} for the {@link AroundEndFinder}
     */
    @FactoryMethod
    public AroundEndFinder(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public void findEntities(@NotNull Instance instance, @NotNull Pos start,
        @NotNull Point end, @NotNull Consumer<? super LivingEntity> callback) {
        instance.getEntityTracker().nearbyEntities(end, data.range, EntityTracker.Target.LIVING_ENTITIES, callback::accept);
    }

    /**
     * Data for an {@link AroundEndFinder}.
     *
     * @param range The euclidean distance range to search for entities
     */
    @DataObject
    public record Data(double range) {

    }
}