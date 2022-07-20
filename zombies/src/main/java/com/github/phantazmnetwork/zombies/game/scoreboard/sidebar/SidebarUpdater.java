package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar;

import com.github.phantazmnetwork.zombies.game.ZombiesScene;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.section.SidebarSection;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.kyori.adventure.text.Component;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SidebarUpdater {

    private final Sidebar sidebar;

    private final List<SidebarSection> sections;

    private final int[] sizes;

    private final Int2ObjectFunction<String> indexToLineId;

    private boolean initialized = false;

    public SidebarUpdater(@NotNull Sidebar sidebar, @NotNull Collection<SidebarSection> sections,
                          @NotNull Int2ObjectFunction<String> indexToLineId) {
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
        this.sections = List.copyOf(sections);
        this.sizes = new int[sections.size()];
        this.indexToLineId = Objects.requireNonNull(indexToLineId, "indexToLineId");
    }

    public void tick(long time) {
        for (int i = 0; i < sections.size(); i++) {
            List<Optional<Component>> newLines = sections.get(i).tick(time);
            for (int j = 0; j < newLines.size(); j++) {
                int index = i + j;
                newLines.get(j).ifPresent(newLine -> {
                    String lineId = indexToLineId.get(index);
                    sidebar.updateLineContent(lineId, newLine);
                });
            }
        }
    }

    private void refreshSections(@NotNull ZombiesScene scene) {
        if (!initialized) {
            for (int i = 0; i < sizes.length; i++) {
                sizes[i] = sections.get(i).getSize(scene);
            }

            for (SidebarSection section : sections) {
                section.invalidateCache();
            }

            initialized = true;
        }
        else {
            boolean shouldInvalidate = false;
            for (int i = 0; i < sizes.length; i++) {
                int newSize = sections.get(i).getSize(scene);
                if (sizes[i] != newSize) {
                    shouldInvalidate = true;
                    sizes[i] = newSize;
                }
            }

            if (shouldInvalidate) {
                for (SidebarSection section : sections) {
                    section.invalidateCache();
                }
            }
        }
    }

}
