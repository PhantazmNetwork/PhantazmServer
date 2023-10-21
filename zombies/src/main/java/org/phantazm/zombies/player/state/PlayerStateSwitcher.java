package org.phantazm.zombies.player.state;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tick.Activable;

public class PlayerStateSwitcher implements Activable {

    private ZombiesPlayerState state;

    public PlayerStateSwitcher() {
    }

    @Override
    public void start() {
        if (state == null) {
            return;
        }

        state.start();
    }

    @Override
    public void tick(long time) {
        if (state == null) {
            return;
        }

        state.tick(time).ifPresent(this::setState);
    }

    @Override
    public void end() {
        if (state == null) {
            return;
        }

        state.end();
    }

    public ZombiesPlayerState getState() {
        return state;
    }

    public void setState(@NotNull ZombiesPlayerState state) {
        if (this.state != null) {
            this.state.end();
        }

        this.state = state;
        this.state.start();
    }
}
