package org.phantazm.zombies.powerup.action;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;

import java.util.Objects;

public abstract class PowerupActionBase implements PowerupAction {
    private final DeactivationPredicate deactivationPredicate;

    public PowerupActionBase(@NotNull DeactivationPredicate deactivationPredicate) {
        this.deactivationPredicate = Objects.requireNonNull(deactivationPredicate, "deactivationPredicate");
    }

    @Override
    public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
        deactivationPredicate.activate(time);
    }

    @Override
    public final @NotNull DeactivationPredicate deactivationPredicate() {
        return deactivationPredicate;
    }
}
