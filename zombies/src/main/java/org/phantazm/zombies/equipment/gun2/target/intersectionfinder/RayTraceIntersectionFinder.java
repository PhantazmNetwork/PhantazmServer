package org.phantazm.zombies.equipment.gun2.target.intersectionfinder;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.RayUtils;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Optional;

public class RayTraceIntersectionFinder implements PlayerComponent<IntersectionFinder> {

    private static final IntersectionFinder FINDER = (entity, start, end, distanceLimitSquared) -> {
        float expand;
        if (entity instanceof LivingEntity livingEntity) {
            expand = livingEntity.getAttributeValue(Attributes.HITBOX_EXPANSION);
        } else {
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
    };

    @Override
    public @NotNull IntersectionFinder forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        return FINDER;
    }
}

