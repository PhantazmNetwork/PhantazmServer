package org.phantazm.zombies.powerup.predicate;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface PickupPredicate {
    boolean canPickup(@NotNull ZombiesPlayer zombiesPlayer);
}
