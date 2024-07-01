package org.phantazm.zombies.sidebar.section;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.sidebar.lineupdater.SidebarLineUpdater;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Model("zombies.sidebar.section.collection")
@Cache(false)
public class CollectionSidebarSection implements SidebarSection {

    private final Collection<SidebarLineUpdater> lineUpdaters;

    @FactoryMethod
    public CollectionSidebarSection(
        @NotNull @Child("lineUpdaterPaths") Collection<? extends SidebarLineUpdater> lineUpdaters) {
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
