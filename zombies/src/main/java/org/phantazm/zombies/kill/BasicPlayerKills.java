package org.phantazm.zombies.kill;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.stats.zombies.ZombiesPlayerMapStats;

import java.util.Objects;

public class BasicPlayerKills implements PlayerKills {

    private final ZombiesPlayerMapStats stats;

    private int kills = 0;

    public BasicPlayerKills(@NotNull ZombiesPlayerMapStats stats) {
        this.stats = Objects.requireNonNull(stats, "stats");
    }

    @Override
    public void onKill(@NotNull PhantazmMob mob) {
        ++kills;
        stats.setKills(stats.getKills() + 1);
    }

    @Override
    public int getKills() {
        return kills;
    }
}
