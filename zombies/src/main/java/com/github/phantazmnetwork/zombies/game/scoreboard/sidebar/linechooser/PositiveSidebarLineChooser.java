package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.linechooser;

import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

public class PositiveSidebarLineChooser implements SidebarLineChooser {

    private static final int MAX_LINES = 15;

    @Override
    public int chooseLine(@NotNull Sidebar sidebar) {
        return MAX_LINES - sidebar.getLines().size();
    }

}
