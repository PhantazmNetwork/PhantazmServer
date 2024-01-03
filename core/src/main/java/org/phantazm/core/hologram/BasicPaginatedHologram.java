package org.phantazm.core.hologram;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.Taggable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class BasicPaginatedHologram implements PaginatedHologram {
    private Instance instance;
    private Point location;

    private final List<ViewableHologram> pages;
    private final Tag<Integer> pageTag;

    private final Object sync;

    public BasicPaginatedHologram(@NotNull Point location, int pageCountHint, @NotNull Tag<Integer> pageTag) {
        this.location = Objects.requireNonNull(location);
        this.pages = new ArrayList<>(pageCountHint);
        this.pageTag = Objects.requireNonNull(pageTag);
        this.sync = new Object();
    }

    public BasicPaginatedHologram(@NotNull Point location, @NotNull Tag<Integer> pageTag) {
        this(location, 5, pageTag);
    }

    private boolean updatePageOnTransition(int oldPage, int newPage) {
        if (newPage != oldPage) {
            return false;
        }

        pages.get(newPage).updateViewableRules();
        pages.get(oldPage).updateViewableRules();
        return true;
    }

    @Override
    public void setInstance(@NotNull Instance instance, @NotNull Point location) {
        synchronized (sync) {
            this.instance = instance;
            this.location = location;

            for (ViewableHologram viewableHologram : pages) {
                viewableHologram.setInstance(instance, location);
            }
        }
    }

    @Override
    public void setInstance(@NotNull Instance instance) {
        Objects.requireNonNull(instance);
        synchronized (sync) {
            if (this.instance == instance) {
                return;
            }

            this.instance = instance;
            for (ViewableHologram viewableHologram : pages) {
                viewableHologram.setInstance(instance, location);
            }
        }
    }

    @Override
    public void setLocation(@NotNull Point location) {
        Objects.requireNonNull(location);
        synchronized (sync) {
            if (this.location.equals(location)) {
                return;
            }

            for (ViewableHologram viewableHologram : this.pages) {
                viewableHologram.setLocation(location);
            }
        }
    }

    public void updatePage(int index, @NotNull Consumer<? super ViewableHologram> pageModifier) {
        synchronized (sync) {
            pageModifier.accept(pages.get(index));
        }
    }

    @Override
    public void addPage(@NotNull List<Component> contents, double gap, Hologram.Alignment alignment) {
        Instance instance;
        ViewableHologram viewableHologram;
        synchronized (sync) {
            instance = this.instance;

            int pageIndex = this.pages.size();
            viewableHologram = new ViewableHologram(location, gap, alignment, player -> {
                return player.getTag(pageTag) == pageIndex;
            });
            viewableHologram.addAll(contents);

            this.pages.add(viewableHologram);
            viewableHologram.setInstance(instance);
        }
    }

    @Override
    public boolean advancePage(@NotNull Taggable taggable) {
        synchronized (sync) {
            if (pages.isEmpty()) {
                return false;
            }

            int pageSize = pages.size();
            int newPage = taggable.tagHandler()
                .updateAndGetTag(pageTag, oldValue -> Math.min(pageSize - 1, oldValue + 1));
            int oldPage = Math.max(0, newPage - 1);
            return updatePageOnTransition(oldPage, newPage);
        }
    }

    @Override
    public boolean retractPage(@NotNull Taggable taggable) {
        synchronized (sync) {
            if (pages.isEmpty()) {
                return false;
            }

            int pageSize = pages.size();
            int newPage = taggable.tagHandler().updateAndGetTag(pageTag, oldValue -> Math.max(0, oldValue - 1));
            int oldPage = Math.min(pageSize - 1, newPage + 1);
            return updatePageOnTransition(oldPage, newPage);
        }
    }

    @Override
    public int pageCount() {
        return pages.size();
    }

    @Override
    public @NotNull Point location() {
        return location;
    }

    @Override
    public void clear() {
        synchronized (sync) {
            for (ViewableHologram hologram : pages) {
                hologram.clear();
            }

            pages.clear();
        }
    }
}
