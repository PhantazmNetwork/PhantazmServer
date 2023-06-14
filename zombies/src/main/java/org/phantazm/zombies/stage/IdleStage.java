package org.phantazm.zombies.stage;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.sidebar.SidebarUpdater;

import java.util.*;
import java.util.function.Function;

public class IdleStage implements Stage {

    private final Map<UUID, SidebarUpdater> sidebarUpdaters = new HashMap<>();

    private final Collection<? extends ZombiesPlayer> zombiesPlayers;

    private final Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator;

    public IdleStage(@NotNull Collection<? extends ZombiesPlayer> zombiesPlayers, @NotNull Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator) {
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.sidebarUpdaterCreator = Objects.requireNonNull(sidebarUpdaterCreator, "sidebarUpdaterCreator");
    }

    @Override
    public boolean shouldContinue() {
        return !zombiesPlayers.isEmpty();
    }

    @Override
    public boolean shouldRevert() {
        return false;
    }

    @Override
    public void onJoin(@NotNull ZombiesPlayer zombiesPlayer) {

    }

    @Override
    public void tick(long time) {
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (!zombiesPlayer.hasQuit()) {
                SidebarUpdater sidebarUpdater = sidebarUpdaters.computeIfAbsent(zombiesPlayer.getUUID(), unused -> {
                    return sidebarUpdaterCreator.apply(zombiesPlayer);
                });
                sidebarUpdater.tick(time);
            }
        }
    }

    @Override
    public void end() {
        for (SidebarUpdater sidebarUpdater : sidebarUpdaters.values()) {
            sidebarUpdater.end();
        }
    }

    @Override
    public boolean hasPermanentPlayers() {
        return false;
    }

    @Override
    public @NotNull Key key() {
        return StageKeys.IDLE_STAGE;
    }
}
