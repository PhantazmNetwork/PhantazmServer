package org.phantazm.zombies.equipment.gun;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.Attributes;

public final class GunUtils {
    private GunUtils() {

    }

    public static float fireRateFactor(@NotNull Entity shooter) {
        float factor = 1F;
        if (shooter instanceof LivingEntity livingEntity) {
            factor = livingEntity.getAttributeValue(Attributes.FIRE_RATE_MULTIPLIER);
        }

        return factor;
    }
}
