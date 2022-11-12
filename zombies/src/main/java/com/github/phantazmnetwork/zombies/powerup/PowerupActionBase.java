package com.github.phantazmnetwork.zombies.powerup;

import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class PowerupActionBase implements PowerupAction {
    private final DeactivationPredicate deactivationPredicate;

    public PowerupActionBase(@NotNull DeactivationPredicate deactivationPredicate) {
        this.deactivationPredicate = Objects.requireNonNull(deactivationPredicate, "deactivationPredicate");
    }

    @Override
    public final @NotNull DeactivationPredicate deactivationPredicate() {
        return deactivationPredicate;
    }

    @Override
    public void activate(@NotNull ZombiesPlayer player, long time) {
        deactivationPredicate.activate(time);
    }
}
