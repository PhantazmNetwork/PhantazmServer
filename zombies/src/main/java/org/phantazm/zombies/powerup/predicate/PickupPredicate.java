package org.phantazm.zombies.powerup.predicate;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;

public interface PickupPredicate {
    boolean canPickup(@NotNull ZombiesPlayer zombiesPlayer, @NotNull Powerup powerup);
}
