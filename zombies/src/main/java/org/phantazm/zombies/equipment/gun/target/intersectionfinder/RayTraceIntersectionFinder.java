package org.phantazm.zombies.equipment.gun.target.intersectionfinder;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.Attributes;
import org.phantazm.core.RayUtils;

import java.util.Optional;

/**
 * An {@link IntersectionFinder} that uses ray tracing to find intersections.
 */
@Model("zombies.gun.intersection_finder.ray_trace")
@Cache
public class RayTraceIntersectionFinder implements IntersectionFinder {

    @FactoryMethod
    public RayTraceIntersectionFinder() {

    }

    @Override
    public @NotNull Optional<Vec> getHitLocation(@NotNull Entity entity, @NotNull Pos start) {
        float expand;
        if (entity instanceof LivingEntity livingEntity) {
            expand = livingEntity.getAttributeValue(Attributes.HITBOX_EXPANSION);
        }
        else {
            expand = 0;
        }

        return RayUtils.rayTrace(entity.getBoundingBox().expand(expand, expand, expand), entity.getPosition(), start);
    }

}
