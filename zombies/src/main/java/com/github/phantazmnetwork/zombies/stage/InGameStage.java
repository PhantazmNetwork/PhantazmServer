package com.github.phantazmnetwork.zombies.stage;

import com.github.phantazmnetwork.commons.Wrapper;
import com.github.phantazmnetwork.zombies.map.RoundHandler;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.player.state.ZombiesPlayerStateKeys;
import com.github.phantazmnetwork.zombies.scoreboard.sidebar.SidebarUpdater;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class InGameStage implements Stage {

    private final Instance instance;

    private final Collection<? extends ZombiesPlayer> zombiesPlayers;

    private final Map<UUID, SidebarUpdater> sidebarUpdaters = new HashMap<>();

    private final Pos spawnPos;

    private final RoundHandler roundHandler;

    private final Wrapper<Long> ticksSinceStart;

    private final Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator;

    public InGameStage(@NotNull Instance instance, @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers,
            @NotNull Pos spawnPos, @NotNull RoundHandler roundHandler, @NotNull Wrapper<Long> ticksSinceStart,
            @NotNull Function<? super ZombiesPlayer, ? extends SidebarUpdater> sidebarUpdaterCreator) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.spawnPos = Objects.requireNonNull(spawnPos, "spawnPos");
        this.roundHandler = Objects.requireNonNull(roundHandler, "roundHandler");
        this.ticksSinceStart = Objects.requireNonNull(ticksSinceStart, "ticksSinceStart");
        this.sidebarUpdaterCreator = Objects.requireNonNull(sidebarUpdaterCreator, "sidebarUpdaterCreator");
    }

    @Override
    public boolean shouldContinue() {
        return roundHandler.hasEnded();
    }

    @Override
    public boolean shouldRevert() {
        return false;
    }

    @Override
    public void onJoin(@NotNull ZombiesPlayer zombiesPlayer) {

    }

    @Override
    public boolean hasPermanentPlayers() {
        return true;
    }

    @Override
    public void start() {
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            zombiesPlayer.getPlayer().ifPresent(player -> {
                player.teleport(spawnPos);
            });
        }
        ticksSinceStart.set(0L);
        if (roundHandler.roundCount() > 0) {
            roundHandler.setCurrentRound(0);
        }
    }

    @Override
    public void tick(long time) {
        ticksSinceStart.apply(ticks -> ticks - 1);
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            SidebarUpdater sidebarUpdater =
                    sidebarUpdaters.computeIfAbsent(zombiesPlayer.getModule().getPlayerView().getUUID(), unused -> {
                        return sidebarUpdaterCreator.apply(zombiesPlayer);
                    });
            sidebarUpdater.tick(time);
        }
    }

    @Override
    public void end() {
        boolean anyAlive = false;
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (zombiesPlayer.isState(ZombiesPlayerStateKeys.ALIVE)) {
                anyAlive = true;
                break;
            }
        }

        if (anyAlive) {
            instance.sendMessage(Component.text("You won"));
        }
        else {
            instance.sendMessage(Component.text("You lost"));
        }

        for (SidebarUpdater sidebarUpdater : sidebarUpdaters.values()) {
            sidebarUpdater.end();
        }
    }
}
