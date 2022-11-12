package com.github.phantazmnetwork.zombies.powerup;

import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

public interface PowerupAction {
    void activate(@NotNull ZombiesPlayer player, long time);

    void deactivate();

    @NotNull DeactivationPredicate deactivationPredicate();
}
