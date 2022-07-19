package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar;

import com.github.phantazmnetwork.zombies.game.ZombiesSceneState;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.linechooser.SidebarLineChooser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

public class ElapsedTimeSidebarUpdater extends SidebarUpdaterBase {

    public ElapsedTimeSidebarUpdater(@NotNull String lineName, @NotNull SidebarLineChooser lineChooser) {
        super(lineName, lineChooser);
    }

    public ElapsedTimeSidebarUpdater(@NotNull SidebarLineChooser lineChooser) {
        this("elapsed-time-display", lineChooser);
    }

    @Override
    public void tick(@NotNull ZombiesSceneState state, @NotNull Sidebar sidebar) {
        // no variable caching since time is expected to change each tick
        long elapsedSeconds = state.getTicksSinceStart() / MinecraftServer.TICK_PER_SECOND;
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;
        Component message =
                Component.text(String.format("%d:%02d:%02d", hours, minutes, seconds), NamedTextColor.GREEN);

        sidebar.updateLineContent(getLineName(), Component.textOfChildren(Component.text("Time: "), message));
    }
}
