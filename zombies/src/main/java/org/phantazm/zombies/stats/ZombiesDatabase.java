package org.phantazm.zombies.stats;

import org.jetbrains.annotations.NotNull;

public interface ZombiesDatabase {

    void synchronize(@NotNull ZombiesPlayerMapStats stats);

}
