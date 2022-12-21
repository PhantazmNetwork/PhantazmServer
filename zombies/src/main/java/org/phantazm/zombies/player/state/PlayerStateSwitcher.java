package org.phantazm.zombies.player.state;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;

import java.util.Objects;

public class PlayerStateSwitcher implements Activable {

    private ZombiesPlayerState state;

    public PlayerStateSwitcher(@NotNull ZombiesPlayerState defaultState) {
        this.state = Objects.requireNonNull(defaultState, "defaultState");
    }

    @Override
    public void start() {
        state.start();
    }

    @Override
    public void tick(long time) {
        state.tick(time).ifPresent(this::setState);
    }

    @Override
    public void end() {
        state.end();
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
