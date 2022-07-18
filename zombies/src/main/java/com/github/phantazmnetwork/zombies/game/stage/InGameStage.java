package com.github.phantazmnetwork.zombies.game.stage;

import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class InGameStage implements Stage {

    private final Map<UUID, ZombiesPlayer> zombiesPlayers;

    private final ZombiesMap map;

    public InGameStage(@NotNull Map<UUID, ZombiesPlayer> zombiesPlayers, @NotNull ZombiesMap map) {
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.map = Objects.requireNonNull(map, "map");
    }

    @Override
    public void tick(long time) {
        map.tick(time);
    }

    @Override
    public void start() {
        map.startRound(0);
    }

    @Override
    public void end() {

    }

    @Override
    public boolean shouldEnd() {
        return map.currentRound() != null;
    }

    @Override
    public boolean hasPermanentPlayers() {
        return true;
    }
}
