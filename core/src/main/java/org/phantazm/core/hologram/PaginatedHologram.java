package org.phantazm.core.hologram;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Taggable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A hologram, bound to a single {@link Instance} with player-specific pages that can be switched between.
 */
public interface PaginatedHologram {
    record PageLine(String format,
        Component component) {
        public PageLine {
            if (format == null) {
                Objects.requireNonNull(component);
            } else if (component == null) {
                Objects.requireNonNull(format);
            } else {
                throw new IllegalArgumentException("Cannot define both a format string and component");
            }
        }

        public boolean isFormat() {
            return format != null;
        }

        public boolean isComponent() {
            return component != null;
        }
    }

    static @NotNull PageLine line(@NotNull String format) {
        return new PageLine(format, null);
    }

    static @NotNull PageLine line(@NotNull Component component) {
        return new PageLine(null, component);
    }

    void setInstance(@NotNull Instance instance, @NotNull Point location);

    void setInstance(@NotNull Instance instance);

    void setLocation(@NotNull Point location);

    boolean advancePage(@NotNull Taggable taggable);

    boolean retractPage(@NotNull Taggable taggable);

    boolean setPage(@NotNull Taggable taggable, int page);

    void addPage(@NotNull List<PageLine> contents, double gap, Hologram.@NotNull Alignment alignment,
        @NotNull ViewableHologram.LineFormatter lineFormatter);

    default void addPage(@NotNull List<PageLine> contents, double gap, Hologram.@NotNull Alignment alignment) {
        addPage(contents, gap, alignment, ViewableHologram.DEFAULT_LINE_FORMATTER);
    }

    void updatePage(int index, @NotNull Consumer<? super @NotNull ViewableHologram> pageModifier);

    int pageCount();

    @NotNull Point location();

    void clear();
}
