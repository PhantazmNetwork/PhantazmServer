package org.phantazm.zombies.powerup2.action;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup2.Powerup;
import org.phantazm.zombies.powerup2.predicate.DeactivationPredicate;

public interface PowerupAction extends Tickable {
    void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time);

    void deactivate(@NotNull ZombiesPlayer player);

    @NotNull DeactivationPredicate deactivationPredicate();
}
