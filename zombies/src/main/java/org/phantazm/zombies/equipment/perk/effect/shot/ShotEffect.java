package org.phantazm.zombies.equipment.perk.effect.shot;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface ShotEffect {
    default void perform(@NotNull Entity entity, @NotNull ZombiesPlayer player) {
        perform(entity, player, 1.0D);
    }

    void perform(@NotNull Entity entity, @NotNull ZombiesPlayer zombiesPlayer, double scale);
}
