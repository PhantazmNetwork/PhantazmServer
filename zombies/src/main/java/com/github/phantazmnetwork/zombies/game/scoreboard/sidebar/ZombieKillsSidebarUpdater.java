package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar;

import com.github.phantazmnetwork.zombies.game.ZombiesSceneState;
import com.github.phantazmnetwork.zombies.game.kill.PlayerKills;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.linechooser.SidebarLineChooser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ZombieKillsSidebarUpdater extends SidebarUpdaterBase {

    private final PlayerKills playerKills;

    private int killCount = -1;

    public ZombieKillsSidebarUpdater(@NotNull String lineName, @NotNull SidebarLineChooser lineChooser,
                                     @NotNull PlayerKills playerKills) {
        super(lineName, lineChooser);
        this.playerKills = Objects.requireNonNull(playerKills, "playerKills");
    }

    public ZombieKillsSidebarUpdater(@NotNull SidebarLineChooser lineChooser, @NotNull PlayerKills playerKills) {
        this("zombie-kills-display", lineChooser, playerKills);
    }

    @Override
    public void tick(@NotNull ZombiesSceneState state, @NotNull Sidebar sidebar) {
        if (killCount == -1 || killCount != playerKills.getKills()) {
            killCount = playerKills.getKills();
            Component message = Component.textOfChildren(Component.text("Zombie Kills: "),
                                                         Component.text(killCount, NamedTextColor.GREEN)
            );
            sidebar.updateLineContent(getLineName(), message);
        }
    }
}
