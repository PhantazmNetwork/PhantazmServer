package org.phantazm.zombies.powerup.predicate;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;

import java.util.OptionalInt;

public interface DeactivationPredicate {
    void activate(@NotNull Powerup powerup, @Nullable ZombiesPlayer zombiesPlayer, long time);

    boolean shouldDeactivate(long time);

    @NotNull OptionalInt duration(@NotNull Powerup powerup, @Nullable ZombiesPlayer zombiesPlayer);
}
