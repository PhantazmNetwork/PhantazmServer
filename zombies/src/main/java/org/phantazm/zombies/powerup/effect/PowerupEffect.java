package org.phantazm.zombies.powerup.effect;

import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public interface PowerupEffect {
    void apply(@NotNull LivingEntity entity);
}
