package com.github.phantazmnetwork.zombies.game.stage;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class IdleStage extends StageBase {

    private final Collection<ZombiesPlayer> zombiesPlayers;

    public IdleStage(@NotNull Collection<Activable> activables, @NotNull Collection<ZombiesPlayer> zombiesPlayers) {
        super(activables);
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
    }

    @Override
    public boolean shouldEnd() {
        return zombiesPlayers.size() != 0;
    }

    @Override
    public boolean hasPermanentPlayers() {
        return false;
    }
}
