package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar;

import com.github.phantazmnetwork.zombies.game.ZombiesSceneState;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

public interface SidebarUpdater {
    void initializeSidebar(@NotNull Sidebar sidebar);

    void tick(@NotNull ZombiesSceneState state, @NotNull Sidebar sidebar);
}
