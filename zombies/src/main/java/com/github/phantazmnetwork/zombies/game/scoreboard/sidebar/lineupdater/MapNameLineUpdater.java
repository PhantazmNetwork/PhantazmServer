package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class MapNameLineUpdater implements SidebarLineUpdater {

    private final Component mapName;

    private boolean mapNameSet = false;

    public MapNameLineUpdater(@NotNull Component mapName) {
        this.mapName = Objects.requireNonNull(mapName, "mapName");
    }

    @Override
    public void invalidateCache() {
        mapNameSet = false;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        if (!mapNameSet) {
            mapNameSet = true;
            return Optional.of(mapName);
        }

        return Optional.empty();
    }
}
