package org.phantazm.stats.zombies;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ZombiesSQLFetcher {

    void synchronizeZombiesPlayerMapStats(@NotNull Connection connection, @NotNull ZombiesPlayerMapStats mapStats);

    @NotNull List<BestTime> getBestTimes(@NotNull Connection connection, @NotNull Key mapKey) throws SQLException;

}
