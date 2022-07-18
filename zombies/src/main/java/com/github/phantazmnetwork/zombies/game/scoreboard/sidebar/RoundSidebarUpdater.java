package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar;

import com.github.phantazmnetwork.zombies.game.ZombiesSceneState;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.linechooser.SidebarLineChooser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RoundSidebarUpdater extends SidebarUpdaterBase {

    private static final String ROUND_LINE_NAME = "round-display";

    private final ZombiesMap map;

    private int lastRoundIndex = -1;

    public RoundSidebarUpdater(@NotNull SidebarLineChooser lineChooser, @NotNull ZombiesMap map) {
        super(ROUND_LINE_NAME, lineChooser);
        this.map = Objects.requireNonNull(map, "map");
    }

    @Override
    public void tick(@NotNull ZombiesSceneState state, @NotNull Sidebar sidebar) {
        if (lastRoundIndex == -1 || lastRoundIndex != map.getRoundIndex()) {
            lastRoundIndex = map.getRoundIndex();
            Component message = Component.text("Round " + lastRoundIndex + 1, NamedTextColor.RED);
            sidebar.updateLineContent(ROUND_LINE_NAME, message);
        }
    }

}
