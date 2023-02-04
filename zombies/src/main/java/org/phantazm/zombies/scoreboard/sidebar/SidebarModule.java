package org.phantazm.zombies.scoreboard.sidebar;

import com.github.steanky.element.core.annotation.Depend;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.dependency.DependencyModule;
import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Memoize
@Depend
@SuppressWarnings("ClassCanBeRecord")
public class SidebarModule implements DependencyModule {

    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;

    private final Collection<? extends ZombiesPlayer> zombiesPlayers;

    private final RoundHandler roundHandler;

    private final Wrapper<Long> ticksSinceStart;

    private final Component date;

    private final int maxPlayers;

    public SidebarModule(@NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap,
            @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers, @NotNull RoundHandler roundHandler,
            @NotNull Wrapper<Long> ticksSinceStart, @NotNull Component date, int maxPlayers) {
        this.playerMap = Objects.requireNonNull(playerMap, "playerMap");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.roundHandler = Objects.requireNonNull(roundHandler, "roundHandler");
        this.ticksSinceStart = Objects.requireNonNull(ticksSinceStart, "ticksSinceStart");
        this.date = Objects.requireNonNull(date, "date");
        this.maxPlayers = maxPlayers;
    }

    public @NotNull Map<? super UUID, ? extends ZombiesPlayer> getZombiesPlayerMap() {
        return playerMap;
    }

    public @NotNull Collection<? extends ZombiesPlayer> getZombiesPlayers() {
        return zombiesPlayers;
    }

    public @NotNull RoundHandler getRoundHandler() {
        return roundHandler;
    }

    public @NotNull Wrapper<Long> getTicksSinceStart() {
        return ticksSinceStart;
    }

    public @NotNull Component getDate() {
        return date;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

}
