package com.github.phantazmnetwork.zombies.kill;

import com.github.phantazmnetwork.mob.PhantazmMob;
import org.jetbrains.annotations.NotNull;

public class BasicPlayerKills implements PlayerKills {

    private int kills = 0;

    @Override
    public void onKill(@NotNull PhantazmMob mob) {
        kills++;
    }

    @Override
    public int getKills() {
        return kills;
    }
}
