package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class ConstantSidebarLineUpdater implements SidebarLineUpdater {

    private final Component component;

    private boolean componentSet = false;

    public ConstantSidebarLineUpdater(@NotNull Component component) {
        this.component = Objects.requireNonNull(component, "component");
    }

    @Override
    public void invalidateCache() {
        componentSet = false;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        if (!componentSet) {
            componentSet = true;
            return Optional.of(component);
        }

        return Optional.empty();
    }
}
