package org.phantazm.core.hologram;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Taggable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * A hologram, bound to a single {@link Instance} with player-specific pages that can be switched between.
 */
public interface PaginatedHologram {
    void setInstance(@NotNull Instance instance, @NotNull Point location);

    void setInstance(@NotNull Instance instance);

    void setLocation(@NotNull Point location);

    boolean advancePage(@NotNull Taggable taggable);

    boolean retractPage(@NotNull Taggable taggable);

    void addPage(@NotNull List<Component> contents, double gap, Hologram.Alignment alignment);

    void updatePage(int index, @NotNull Consumer<? super @NotNull ViewableHologram> pageModifier);

    int pageCount();

    @NotNull Point location();

    void clear();
}
