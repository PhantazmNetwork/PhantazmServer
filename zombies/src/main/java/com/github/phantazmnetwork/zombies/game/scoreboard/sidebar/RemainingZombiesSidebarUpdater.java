package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar;

import com.github.phantazmnetwork.zombies.game.ZombiesSceneState;
import com.github.phantazmnetwork.zombies.game.map.Round;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.linechooser.SidebarLineChooser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RemainingZombiesSidebarUpdater extends SidebarUpdaterBase {

    private static final String REMAINING_ZOMBIES_LINE_NAME = "remaining-zombies-display";

    private final ZombiesMap map;

    private int lastRemainingZombies = -1;

    public RemainingZombiesSidebarUpdater(@NotNull SidebarLineChooser lineChooser, @NotNull ZombiesMap map) {
        super(REMAINING_ZOMBIES_LINE_NAME, lineChooser);
        this.map = Objects.requireNonNull(map, "map");
    }

    @Override
    public void tick(@NotNull ZombiesSceneState state, @NotNull Sidebar sidebar) {
        Round round = map.currentRound();
        if (round != null && (lastRemainingZombies == -1 || lastRemainingZombies != round.getTotalMobCount())) {
            lastRemainingZombies = round.getTotalMobCount();
            Component message = Component.textOfChildren(Component.text("Remaining Zombies: "),
                                                         Component.text(lastRemainingZombies, NamedTextColor.GREEN)
            );
            sidebar.updateLineContent(REMAINING_ZOMBIES_LINE_NAME, message);
        }
    }
}
