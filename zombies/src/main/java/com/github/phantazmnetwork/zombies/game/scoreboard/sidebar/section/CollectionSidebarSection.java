package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.section;

import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater.SidebarLineUpdater;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class CollectionSidebarSection implements SidebarSection {

    private final Collection<SidebarLineUpdater> lineUpdaters;

    public CollectionSidebarSection(@NotNull Collection<SidebarLineUpdater> lineUpdaters) {
        this.lineUpdaters = List.copyOf(lineUpdaters);
    }

    @Override
    public void invalidateCache() {
        for (SidebarLineUpdater lineUpdater : lineUpdaters) {
            lineUpdater.invalidateCache();
        }
    }

    @Override
    public int getSize() {
        return lineUpdaters.size();
    }

    @Override
    public @NotNull List<Optional<Component>> update(long time) {
        List<Optional<Component>> updates = new ArrayList<>(lineUpdaters.size());
        for (SidebarLineUpdater lineUpdater : lineUpdaters) {
            updates.add(lineUpdater.tick(time));
        }
        return updates;
    }
}
