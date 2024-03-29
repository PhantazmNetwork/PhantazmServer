package org.phantazm.zombies.powerup.action;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;
import org.phantazm.zombies.powerup.predicate.ImmediateDeactivationPredicate;

public abstract class InstantAction implements PowerupAction {
    @Override
    public void deactivate(@NotNull ZombiesPlayer player) {

    }

    @Override
    public @NotNull DeactivationPredicate deactivationPredicate() {
        return ImmediateDeactivationPredicate.INSTANCE;
    }
}
