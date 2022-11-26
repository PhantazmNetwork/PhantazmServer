package com.github.phantazmnetwork.zombies.scoreboard.sidebar;

import com.github.phantazmnetwork.commons.Wrapper;
import com.github.phantazmnetwork.zombies.map.RoundHandler;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.steanky.element.core.annotation.DependencySupplier;
import com.github.steanky.element.core.dependency.DependencyModule;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class SidebarModule implements DependencyModule {

    private final Collection<? extends ZombiesPlayer> zombiesPlayers;

    private final RoundHandler roundHandler;

    private final Wrapper<Long> ticksSinceStart;

    private final Component date;

    private final int maxPlayers;

    public SidebarModule(@NotNull Collection<? extends ZombiesPlayer> zombiesPlayers,
            @NotNull RoundHandler roundHandler, @NotNull Wrapper<Long> ticksSinceStart, @NotNull Component date,
            int maxPlayers) {
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.roundHandler = Objects.requireNonNull(roundHandler, "roundHandler");
        this.ticksSinceStart = Objects.requireNonNull(ticksSinceStart, "ticksSinceStart");
        this.date = Objects.requireNonNull(date, "date");
        this.maxPlayers = maxPlayers;
    }

    @DependencySupplier("zombies.dependency.sidebar.player_collection")
    public @NotNull Collection<? extends ZombiesPlayer> getZombiesPlayers() {
        return zombiesPlayers;
    }

    @DependencySupplier("zombies.dependency.sidebar.round_handler")
    public @NotNull RoundHandler getRoundHandler() {
        return roundHandler;
    }

    @DependencySupplier("zombies.dependency.sidebar.ticks_since_start")
    public @NotNull Wrapper<Long> getTicksSinceStart() {
        return ticksSinceStart;
    }

    @DependencySupplier("zombies.dependency.sidebar.date")
    public @NotNull Component getDate() {
        return date;
    }

    @DependencySupplier("zombies.dependency.sidebar.max_players")
    public int getMaxPlayers() {
        return maxPlayers;
    }

}
