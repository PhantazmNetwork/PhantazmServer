package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.section;

import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater.SidebarLineUpdater;
import com.github.steanky.element.core.annotation.DataName;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Model("zombies.sidebar.section.collection")
public class CollectionSidebarSection implements SidebarSection {

    private final Collection<SidebarLineUpdater> lineUpdaters;

    @DataObject
    public record Data(@NotNull @DataName("line_updaters") Collection<String> lineUpdaterPaths) {

        public Data {
            Objects.requireNonNull(lineUpdaterPaths, "lineUpdaters");
        }

    }

    @FactoryMethod
    public CollectionSidebarSection(@NotNull Data data,
            @NotNull @DataName("line_updaters") Collection<? extends SidebarLineUpdater> lineUpdaters) {
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
