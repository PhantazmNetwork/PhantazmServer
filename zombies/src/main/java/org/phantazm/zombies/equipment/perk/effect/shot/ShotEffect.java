package org.phantazm.zombies.equipment.perk.effect.shot;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface ShotEffect {
    void perform(@NotNull Entity entity, @NotNull ZombiesPlayer player);
}
