package com.github.phantazmnetwork.zombies.game.player.state;

import com.github.phantazmnetwork.commons.Tickable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlayerStateSwitcher implements Tickable {

    private ZombiesPlayerState state;

    public PlayerStateSwitcher(@NotNull ZombiesPlayerState defaultState) {
        this.state = Objects.requireNonNull(defaultState, "defaultState");
    }

    public void start() {
        state.start();
    }

    @Override
    public void tick(long time) {
        state.tick(time).ifPresent(this::setState);
    }

    public @NotNull ZombiesPlayerState getState() {
        return state;
    }

    public void setState(@NotNull ZombiesPlayerState state) {
        this.state.end();
        this.state = state;
        this.state.start();
    }
}
