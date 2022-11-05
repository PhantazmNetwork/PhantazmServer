package com.github.phantazmnetwork.zombies.game.powerup;

import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

public interface PowerupAction {
    void activate(@NotNull ZombiesPlayer player, long time);

    void deactivate();

    @NotNull DeactivationPredicate deactivationPredicate();
}
