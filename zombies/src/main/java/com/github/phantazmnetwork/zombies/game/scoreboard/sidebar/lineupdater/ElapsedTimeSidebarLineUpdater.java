package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater;

import com.github.phantazmnetwork.zombies.game.stage.InGameStage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class ElapsedTimeSidebarLineUpdater implements SidebarLineUpdater {

    private final InGameStage inGameStage;

    public ElapsedTimeSidebarLineUpdater(@NotNull InGameStage inGameStage) {
        this.inGameStage = Objects.requireNonNull(inGameStage, "inGameStage");
    }

    @Override
    public void invalidateCache() {

    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        // no variable caching since time is expected to change each tick
        long elapsedSeconds = inGameStage.getTicksSinceStart() / MinecraftServer.TICK_PER_SECOND;
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;
        return Optional.of(
                Component.text(String.format("%d:%02d:%02d", hours, minutes, seconds), NamedTextColor.GREEN));
    }
}
