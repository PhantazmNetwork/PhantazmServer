package com.github.phantazmnetwork.zombies.scoreboard.sidebar.lineupdater;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface SidebarLineUpdater {

    void invalidateCache();

    @NotNull Optional<Component> tick(long time);
}
