package org.phantazm.zombies.powerup.action;

import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;

public interface PowerupAction extends Tickable {
    void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time);

    void deactivate(@NotNull ZombiesPlayer player);

    @NotNull
    DeactivationPredicate deactivationPredicate();

    default void tick(long time) {
        
    }
}
