package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.section;

import com.github.phantazmnetwork.zombies.game.ZombiesScene;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater.SidebarLineUpdater;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SingleSidebarSection implements SidebarSection {

    private final SidebarLineUpdater sidebarLineUpdater;

    public SingleSidebarSection(@NotNull SidebarLineUpdater sidebarLineUpdater) {
        this.sidebarLineUpdater = Objects.requireNonNull(sidebarLineUpdater, "sidebarLineUpdater");
    }

    @Override
    public void invalidateCache() {
        sidebarLineUpdater.invalidateCache();
    }

    @Override
    public int getSize(@NotNull ZombiesScene scene) {
        return 1;
    }

    @Override
    public @NotNull List<Optional<Component>> tick(long time, @NotNull ZombiesScene scene) {
        return Collections.singletonList(sidebarLineUpdater.tick(time, scene));
    }
}
