package com.github.phantazmnetwork.zombies.kill;

import com.github.phantazmnetwork.mob.PhantazmMob;
import org.jetbrains.annotations.NotNull;

public interface PlayerKills {

    void onKill(@NotNull PhantazmMob mob);

    int getKills();

}
