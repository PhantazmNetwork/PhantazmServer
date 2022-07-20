package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public class ConditionalSidebarLineUpdater implements SidebarLineUpdater {

    private final List<Pair<BooleanSupplier, SidebarLineUpdater>> subUpdaters;

    public ConditionalSidebarLineUpdater(@NotNull List<Pair<BooleanSupplier, SidebarLineUpdater>> subUpdaters) {
        this.subUpdaters = List.copyOf(subUpdaters);
    }

    @Override
    public void invalidateCache() {
        for (Pair<BooleanSupplier, SidebarLineUpdater> subUpdater : subUpdaters) {
            subUpdater.right().invalidateCache();
        }
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        for (Pair<BooleanSupplier, SidebarLineUpdater> subUpdater : subUpdaters) {
            if (subUpdater.left().getAsBoolean()) {
                return subUpdater.right().tick(time);
            }
        }

        return Optional.empty();
    }
}
