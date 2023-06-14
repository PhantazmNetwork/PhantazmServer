package org.phantazm.zombies.equipment.gun.target.intersectionfinder;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.RayUtils;
import org.phantazm.zombies.Attributes;

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
    public @NotNull Optional<Vec> getHitLocation(@NotNull Entity entity, @NotNull Pos start, @NotNull Point end,
            double distanceLimitSquared) {
        float expand;
        if (entity instanceof LivingEntity livingEntity) {
            expand = livingEntity.getAttributeValue(Attributes.HITBOX_EXPANSION);
        }
        else {
            expand = 0;
        }

        BoundingBox boundingBox = entity.getBoundingBox().expand(expand, expand, expand);
        Optional<Vec> hitOptional = RayUtils.rayTrace(boundingBox, entity.getPosition(), start);
        if (hitOptional.isPresent()) {
            Vec hit = hitOptional.get();
            if (start.distanceSquared(hit) > distanceLimitSquared) {
                return Optional.empty();
            }
        }

        return hitOptional;
    }

}
