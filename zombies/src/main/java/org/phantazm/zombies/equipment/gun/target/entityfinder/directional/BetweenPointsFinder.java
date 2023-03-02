package org.phantazm.zombies.equipment.gun.target.entityfinder.directional;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A {@link DirectionalEntityFinder} that finds entities between the start and end point of a shot.
 */
@Model("zombies.gun.entity_finder.directional.between_points")
@Cache
public class BetweenPointsFinder implements DirectionalEntityFinder {

    @FactoryMethod
    public BetweenPointsFinder() {

    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull Collection<LivingEntity> findEntities(@NotNull Instance instance, @NotNull Pos start,
            @NotNull Point end) {
        Collection<LivingEntity> entities = new ArrayList<>();
        instance.getEntityTracker().raytraceCandidates(start, end, EntityTracker.Target.LIVING_ENTITIES, entities::add);

        return entities;
    }

}
