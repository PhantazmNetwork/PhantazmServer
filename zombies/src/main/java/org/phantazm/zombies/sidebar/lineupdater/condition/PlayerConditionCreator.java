package org.phantazm.zombies.sidebar.lineupdater.condition;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.function.BooleanSupplier;

public interface PlayerConditionCreator {
    @NotNull BooleanSupplier forPlayer(@NotNull ZombiesPlayer zombiesPlayer);
}
