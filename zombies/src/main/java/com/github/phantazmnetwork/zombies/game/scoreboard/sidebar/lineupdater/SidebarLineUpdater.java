package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater;

import com.github.phantazmnetwork.zombies.game.ZombiesScene;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface SidebarLineUpdater {

    void invalidateCache();

    @NotNull Optional<Component> tick(long time, @NotNull ZombiesScene scene);
}
