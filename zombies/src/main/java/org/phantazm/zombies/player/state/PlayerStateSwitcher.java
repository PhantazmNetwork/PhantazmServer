package org.phantazm.zombies.player.state;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.stats.zombies.ZombiesPlayerMapStats;

import java.util.Objects;

public class PlayerStateSwitcher implements Activable {

    private final ZombiesPlayerMapStats stats;

    private ZombiesPlayerState state;

    public PlayerStateSwitcher(@NotNull ZombiesPlayerMapStats stats) {
        this.stats = Objects.requireNonNull(stats, "stats");
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

    public @NotNull ZombiesPlayerState getState() {
        return state;
    }

    public void setState(@NotNull ZombiesPlayerState state) {
        if (this.state != null) {
            this.state.end();
        }

        if (state.key().equals(ZombiesPlayerStateKeys.KNOCKED.key())) {
            stats.setKnocks(stats.getKnocks() + 1);
        } else if (state.key().equals(ZombiesPlayerStateKeys.DEAD.key())) {
            stats.setDeaths(stats.getDeaths() + 1);
        }

        this.state = state;
        this.state.start();
    }
}
