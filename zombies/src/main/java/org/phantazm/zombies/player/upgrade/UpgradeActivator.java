package org.phantazm.zombies.player.upgrade;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface UpgradeActivator {
    void hook();

    void refresh(@NotNull ZombiesPlayer zombiesPlayer);
}
