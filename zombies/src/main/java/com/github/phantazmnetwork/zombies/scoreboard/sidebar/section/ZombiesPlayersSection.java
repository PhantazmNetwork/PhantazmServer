package com.github.phantazmnetwork.zombies.scoreboard.sidebar.section;

import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.scoreboard.sidebar.lineupdater.SidebarLineUpdater;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class ZombiesPlayersSection implements SidebarSection {

    private final Collection<? extends ZombiesPlayer> zombiesPlayers;

    private final List<SidebarLineUpdater> lineUpdaters;

    public ZombiesPlayersSection(@NotNull Collection<? extends ZombiesPlayer> zombiesPlayers) {
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.lineUpdaters = new ArrayList<>(zombiesPlayers.size());//TODO: max players
    }

    @Override
    public void invalidateCache() {
        lineUpdaters.clear();
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            lineUpdaters.add(createLineUpdater(zombiesPlayer));
        }
    }

    @Override
    public int getSize() {
        return zombiesPlayers.size();
    }

    @Override
    public @NotNull List<Optional<Component>> update(long time) {
        List<Optional<Component>> updates = new ArrayList<>(lineUpdaters.size());
        for (SidebarLineUpdater lineUpdater : lineUpdaters) {
            updates.add(lineUpdater.tick(time));
        }

        return updates;
    }

    protected abstract @NotNull SidebarLineUpdater createLineUpdater(@NotNull ZombiesPlayer zombiesPlayer);

}
