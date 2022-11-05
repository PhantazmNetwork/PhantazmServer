package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater;

import com.github.phantazmnetwork.zombies.game.map.RoundHandler;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Model("zombies.sidebar.lineupdater.round")
public class RoundSidebarLineUpdater implements SidebarLineUpdater {

    private final Supplier<? extends RoundHandler> roundHandlerSupplier;

    private int lastRoundIndex = -1;

    @FactoryMethod
    public RoundSidebarLineUpdater(@NotNull @Dependency("zombies.dependency.map_object.round_handler_supplier")
    Supplier<? extends RoundHandler> roundHandlerSupplier) {
        this.roundHandlerSupplier = Objects.requireNonNull(roundHandlerSupplier, "roundHandlerSupplier");
    }

    @Override
    public void invalidateCache() {
        lastRoundIndex = -1;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        int newIndex = roundHandlerSupplier.get().currentRoundIndex();
        if ((lastRoundIndex == -1 || lastRoundIndex != newIndex) && newIndex != -1) {
            lastRoundIndex = newIndex;
            return Optional.of(Component.text("Round " + (lastRoundIndex + 1), NamedTextColor.RED));
        }

        return Optional.empty();
    }

}
