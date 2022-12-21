package org.phantazm.zombies.powerup;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface PowerupAction {
    void activate(@NotNull ZombiesPlayer player, long time);

    void deactivate(@NotNull ZombiesPlayer player);

    @NotNull DeactivationPredicate deactivationPredicate();
}
