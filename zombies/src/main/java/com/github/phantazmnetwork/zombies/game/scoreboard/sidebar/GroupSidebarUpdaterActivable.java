package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public abstract class GroupSidebarUpdaterActivable implements Activable {

    private final Collection<ZombiesPlayer> players;

    private final Collection<SidebarUpdater> sidebarUpdaters;

    public GroupSidebarUpdaterActivable(@NotNull Collection<ZombiesPlayer> players, int maxPlayerCount) {
        this.players = Objects.requireNonNull(players, "players");
        this.sidebarUpdaters = new ArrayList<>(maxPlayerCount);
    }

    @Override
    public void start() {
        for (ZombiesPlayer zombiesPlayer : players) {
            sidebarUpdaters.add(createUpdater(zombiesPlayer));
        }
    }

    @Override
    public void tick(long time) {
        for (SidebarUpdater sidebarUpdater : sidebarUpdaters) {
            sidebarUpdater.tick(time);
        }
    }

    @Override
    public void end() {
        for (SidebarUpdater sidebarUpdater : sidebarUpdaters) {
            sidebarUpdater.end();
        }
        sidebarUpdaters.clear();
    }

    protected abstract @NotNull SidebarUpdater createUpdater(@NotNull ZombiesPlayer zombiesPlayer);

}
