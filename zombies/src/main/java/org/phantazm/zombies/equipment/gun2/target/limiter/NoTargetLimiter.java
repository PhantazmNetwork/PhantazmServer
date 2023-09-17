package org.phantazm.zombies.equipment.gun2.target.limiter;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

public class NoTargetLimiter implements PlayerComponent<TargetLimiter> {

    private static final TargetLimiter LIMITER = (start, targets) -> targets;

    @Override
    public @NotNull TargetLimiter forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        return LIMITER;
    }
}
