package org.phantazm.zombies.stage;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

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

    @Override
    public @NotNull Key key() {
        return StageKeys.IDLE_STAGE;
    }
}
