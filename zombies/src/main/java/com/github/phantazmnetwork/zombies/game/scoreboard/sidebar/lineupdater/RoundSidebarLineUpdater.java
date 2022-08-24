package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater;

import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class RoundSidebarLineUpdater implements SidebarLineUpdater {

    private final ZombiesMap map;

    private int lastRoundIndex = -1;

    public RoundSidebarLineUpdater(@NotNull ZombiesMap map) {
        this.map = Objects.requireNonNull(map, "map");
    }

    @Override
    public void invalidateCache() {
        lastRoundIndex = -1;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        int newIndex = map.getRoundIndex();
        if ((lastRoundIndex == -1 || lastRoundIndex != newIndex) && newIndex != -1) {
            lastRoundIndex = newIndex;
            return Optional.of(Component.text("Round " + (lastRoundIndex + 1), NamedTextColor.RED));
        }

        return Optional.empty();
    }

}
