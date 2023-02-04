package org.phantazm.zombies.scoreboard.sidebar.lineupdater;

import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.handler.RoundHandler;

import java.util.Objects;
import java.util.Optional;

@Model("zombies.sidebar.line_updater.round")
public class RoundSidebarLineUpdater implements SidebarLineUpdater {

    private final RoundHandler roundHandler;

    private int lastRoundIndex = -1;

    @FactoryMethod
    public RoundSidebarLineUpdater(@NotNull RoundHandler roundHandler) {
        this.roundHandler = Objects.requireNonNull(roundHandler, "roundHandler");
    }

    @Override
    public void invalidateCache() {
        lastRoundIndex = -1;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        int newIndex = roundHandler.currentRoundIndex();
        if ((lastRoundIndex == -1 || lastRoundIndex != newIndex) && newIndex != -1) {
            lastRoundIndex = newIndex;
            return Optional.of(Component.text("Round " + (lastRoundIndex + 1), NamedTextColor.RED));
        }

        return Optional.empty();
    }

}
