package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar;

import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.linechooser.SidebarLineChooser;
import net.kyori.adventure.text.Component;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class SidebarUpdaterBase implements SidebarUpdater {

    private final String lineName;

    private final SidebarLineChooser lineChooser;

    public SidebarUpdaterBase(@NotNull String lineName, @NotNull SidebarLineChooser lineChooser) {
        this.lineName = Objects.requireNonNull(lineName, "lineName");
        this.lineChooser = Objects.requireNonNull(lineChooser, "lineChooser");
    }

    @Override
    public void initializeSidebar(@NotNull Sidebar sidebar) {
        int line = lineChooser.chooseLine(sidebar);
        sidebar.createLine(new Sidebar.ScoreboardLine(lineName, Component.empty(), line));
    }

    protected @NotNull String getLineName() {
        return lineName;
    }

}
