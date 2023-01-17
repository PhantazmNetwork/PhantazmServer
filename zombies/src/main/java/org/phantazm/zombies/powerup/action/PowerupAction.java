package org.phantazm.zombies.powerup.action;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;

public interface PowerupAction {
    void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time);

    void deactivate(@NotNull ZombiesPlayer player);

    @NotNull DeactivationPredicate deactivationPredicate();
}
