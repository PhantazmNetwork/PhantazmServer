package org.phantazm.zombies.scoreboard.sidebar;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.text.Component;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.zombies.scoreboard.sidebar.section.SidebarSection;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Model("zombies.sidebar.updater")
public class SidebarUpdater implements Activable {
    private static final int MAX_SIDEBAR_ROWS = 15;

    private final Sidebar sidebar;
    private final List<SidebarSection> sections;
    private final int[] sizes;
    private int totalSize = 0;

    @FactoryMethod
    public SidebarUpdater(@NotNull Sidebar sidebar, @NotNull @Child("sections") Collection<SidebarSection> sections) {
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
        this.sections = List.copyOf(sections);
        this.sizes = new int[sections.size()];
    }

    @Override
    public void start() {
        for (int i = 0; i < sizes.length; i++) {
            int size = sections.get(i).getSize();
            sizes[i] = size;
            totalSize += size;
        }

        for (SidebarSection section : sections) {
            section.invalidateCache();
        }

        int clampedSize = Math.min(totalSize, MAX_SIDEBAR_ROWS);
        for (int i = 0; i < clampedSize; i++) {
            sidebar.createLine(new Sidebar.ScoreboardLine(lineId(i), Component.empty(), clampedSize - i - 1));
        }
    }

    @Override
    public void tick(long time) {
        refreshSections();
        int index = 0;
        for (SidebarSection section : sections) {
            List<Optional<Component>> newLines = section.update(time);

            for (Optional<Component> line : newLines) {
                if (0 <= index && index < MAX_SIDEBAR_ROWS) {
                    int finalIndex = index;
                    line.ifPresent(newLine -> {
                        String lineId = lineId(finalIndex);
                        sidebar.updateLineContent(lineId, newLine);
                    });
                }
                else {
                    return;
                }

                index++;
            }
        }
    }

    @Override
    public void end() {
        for (Sidebar.ScoreboardLine line : sidebar.getLines()) {
            sidebar.removeLine(line.getId());
        }
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

            int oldClampedSize = Math.min(totalSize, MAX_SIDEBAR_ROWS);
            int newClampedSize = Math.min(newTotalSize, MAX_SIDEBAR_ROWS);
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

    @DataObject
    public record Data(@NotNull @ChildPath("sections") Collection<String> sectionPaths) {

        public Data {
            Objects.requireNonNull(sectionPaths, "sectionPaths");
        }

    }

}
