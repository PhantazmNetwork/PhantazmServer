package org.phantazm.zombies.scoreboard.sidebar.lineupdater.creator;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scoreboard.sidebar.lineupdater.SidebarLineUpdater;

public interface PlayerUpdaterCreator {
    @NotNull SidebarLineUpdater forPlayer(@NotNull ZombiesPlayer zombiesPlayer);
}
