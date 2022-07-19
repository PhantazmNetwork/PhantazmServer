package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar;

import com.github.phantazmnetwork.zombies.game.ZombiesScene;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.linechooser.SidebarLineChooser;
import com.github.phantazmnetwork.zombies.game.stage.InGameStage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ElapsedTimeSidebarUpdater extends SidebarUpdaterBase {

    private final InGameStage inGameStage;

    public ElapsedTimeSidebarUpdater(@NotNull String lineName, @NotNull SidebarLineChooser lineChooser,
                                     @NotNull InGameStage inGameStage) {
        super(lineName, lineChooser);
        this.inGameStage = Objects.requireNonNull(inGameStage, "inGameStage");
    }

    public ElapsedTimeSidebarUpdater(@NotNull SidebarLineChooser lineChooser, @NotNull InGameStage inGameStage) {
        this("elapsed-time-display", lineChooser, inGameStage);
    }

    @Override
    public void tick(@NotNull ZombiesScene scene, @NotNull Sidebar sidebar) {
        // no variable caching since time is expected to change each tick
        long elapsedSeconds = inGameStage.getTicksSinceStart() / MinecraftServer.TICK_PER_SECOND;
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;
        Component message =
                Component.text(String.format("%d:%02d:%02d", hours, minutes, seconds), NamedTextColor.GREEN);

        sidebar.updateLineContent(getLineName(), Component.textOfChildren(Component.text("Time: "), message));
    }
}
