package org.phantazm.zombies.equipment.gun;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.AttributeUtils;
import org.phantazm.zombies.Attributes;

public final class GunUtils {
    private GunUtils() {

    }

    public static float exactDelayTicks(long base, @Nullable Entity entityOptional) {
        if (!(entityOptional instanceof LivingEntity livingEntity)) {
            return base;
        }

        return AttributeUtils.computeWithBase(base, livingEntity.getAttribute(Attributes.GUN_FIRE_DELAY));
    }
}
