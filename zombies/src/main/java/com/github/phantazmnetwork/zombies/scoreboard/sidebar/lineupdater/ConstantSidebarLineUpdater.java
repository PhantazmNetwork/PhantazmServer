package com.github.phantazmnetwork.zombies.scoreboard.sidebar.lineupdater;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

@Model("zombies.sidebar.lineupdater.constant")
public class ConstantSidebarLineUpdater implements SidebarLineUpdater {

    @DataObject
    public record Data(@NotNull Component component) {

        public Data {
            Objects.requireNonNull(component, "component");
        }

    }

    private final Data data;

    private boolean componentSet = false;

    @FactoryMethod
    public ConstantSidebarLineUpdater(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public void invalidateCache() {
        componentSet = false;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        if (!componentSet) {
            componentSet = true;
            return Optional.of(data.component());
        }

        return Optional.empty();
    }
}