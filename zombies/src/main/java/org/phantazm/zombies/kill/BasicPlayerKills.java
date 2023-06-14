package org.phantazm.zombies.kill;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;

public class BasicPlayerKills implements PlayerKills {
    private int kills = 0;

    @Override
    public void onKill(@NotNull PhantazmMob mob) {
        ++kills;
    }

    @Override
    public int getKills() {
        return kills;
    }
}
