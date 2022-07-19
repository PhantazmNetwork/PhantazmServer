package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar;

import com.github.phantazmnetwork.zombies.game.ZombiesScene;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.linechooser.SidebarLineChooser;
import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConditionalSidebarUpdater extends SidebarUpdaterBase {

    public interface Condition {
        boolean isSatisfied(@NotNull ZombiesScene scene);
    }

    private final List<Pair<Condition, SidebarUpdaterBase>> subUpdaters;

    public ConditionalSidebarUpdater(@NotNull String lineName, @NotNull SidebarLineChooser lineChooser,
                                     @NotNull List<Pair<Condition, SidebarUpdaterBase>> subUpdaters) {
        super(lineName, lineChooser);
        this.subUpdaters = List.copyOf(subUpdaters);
    }

    @Override
    public void tick(@NotNull ZombiesScene scene, @NotNull Sidebar sidebar) {
        for (Pair<Condition, SidebarUpdaterBase> subUpdater : subUpdaters) {
            if (subUpdater.left().isSatisfied(scene)) {
                subUpdater.right().tick(scene, sidebar);
                break;
            }
        }
    }
}
