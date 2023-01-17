package org.phantazm.zombies.powerup;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

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
