package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar;

import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.section.SidebarSection;
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

    private int totalSize = 0;

    public SidebarUpdater(@NotNull Sidebar sidebar, @NotNull Collection<SidebarSection> sections) {
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
        this.sections = List.copyOf(sections);
        this.sizes = new int[sections.size()];
    }

    public void start() {
        for (int i = 0; i < sizes.length; i++) {
            int size = sections.get(i).getSize();
            sizes[i] = size;
            totalSize += size;
        }

        for (SidebarSection section : sections) {
            section.invalidateCache();
        }

        int clampedSize = Math.min(totalSize, 15);
        for (int i = 0; i < clampedSize; i++) {
            sidebar.createLine(new Sidebar.ScoreboardLine(lineId(i), Component.empty(), clampedSize - i));
        }
    }

    public void tick(long time) {
        refreshSections();
        for (int i = 0; i < sections.size(); i++) {
            List<Optional<Component>> newLines = sections.get(i).update(time);
            for (int j = 0; j < newLines.size(); j++) {
                int index = i + j;
                if (0 <= index && index < 15) {
                    newLines.get(j).ifPresent(newLine -> {
                        String lineId = lineId(index);
                        sidebar.updateLineContent(lineId, newLine);
                    });
                }
            }
        }
    }

    public void end() {

    }

    private void refreshSections() {
        boolean shouldInvalidate = false;
        int newTotalSize = 0;
        for (int i = 0; i < sizes.length; i++) {
            int newSize = sections.get(i).getSize();
            if (sizes[i] != newSize) {
                shouldInvalidate = true;
                sizes[i] = newSize;
            }
            newTotalSize += newSize;
        }

        if (shouldInvalidate) {
            for (SidebarSection section : sections) {
                section.invalidateCache();
            }

            int oldClampedSize = Math.min(totalSize, 15);
            int newClampedSize = Math.min(newTotalSize, 15);
            if (oldClampedSize < newClampedSize) {
                for (int i = oldClampedSize; i < newClampedSize; i++) {
                    sidebar.createLine(new Sidebar.ScoreboardLine(lineId(i), Component.empty(), newClampedSize - i));
                }
            }
            else if (oldClampedSize > newClampedSize) {
                for (int i = oldClampedSize - 1; i >= newClampedSize; i--) {
                    sidebar.removeLine(lineId(i));
                }
            }

            totalSize = newTotalSize;
        }
    }

    private @NotNull String lineId(int index) {
        return "line" + index;
    }

}
