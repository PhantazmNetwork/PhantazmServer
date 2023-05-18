package org.phantazm.zombies.kill;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;

public interface PlayerKills {

    void onKill(@NotNull PhantazmMob mob);

    int getKills();

}
