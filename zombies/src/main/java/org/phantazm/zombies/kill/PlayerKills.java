package org.phantazm.zombies.kill;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;

public interface PlayerKills {

    void onKill(@NotNull Mob mob);

    int getKills();

}
