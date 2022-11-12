package com.github.phantazmnetwork.zombies.stage;

import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class IdleStage implements Stage {

    private final Collection<? extends ZombiesPlayer> zombiesPlayers;

    public IdleStage(@NotNull Collection<? extends ZombiesPlayer> zombiesPlayers) {
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
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
    public boolean hasPermanentPlayers() {
        return false;
    }
}
