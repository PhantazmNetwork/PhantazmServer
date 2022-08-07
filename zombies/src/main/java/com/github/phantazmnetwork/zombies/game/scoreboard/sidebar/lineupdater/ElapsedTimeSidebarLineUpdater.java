package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater;

import com.github.phantazmnetwork.core.time.TickFormatter;
import com.github.phantazmnetwork.zombies.game.stage.InGameStage;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class ElapsedTimeSidebarLineUpdater implements SidebarLineUpdater {

    private final InGameStage inGameStage;

    private final TickFormatter tickFormatter;

    public ElapsedTimeSidebarLineUpdater(@NotNull InGameStage inGameStage, @NotNull TickFormatter tickFormatter) {
        this.inGameStage = Objects.requireNonNull(inGameStage, "inGameStage");
        this.tickFormatter = Objects.requireNonNull(tickFormatter, "tickFormatter");
    }

    @Override
    public void invalidateCache() {

    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        // no variable caching since time is expected to change each tick
        return Optional.of(tickFormatter.format(inGameStage.getTicksSinceStart()));
    }
}
