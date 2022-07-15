package com.github.phantazmnetwork.zombies.game.stage;

import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class InGameStage implements Stage {

    private final Map<UUID, ZombiesPlayer> zombiesPlayers;

    public InGameStage(@NotNull Map<UUID, ZombiesPlayer> zombiesPlayers) {
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
    }

    @Override
    public void tick(long time) {

    }

    @Override
    public void start() {

    }

    @Override
    public void end() {

    }

    @Override
    public boolean shouldEnd() {
        return false;
    }

    @Override
    public boolean hasPermanentPlayers() {
        return true;
    }
}
