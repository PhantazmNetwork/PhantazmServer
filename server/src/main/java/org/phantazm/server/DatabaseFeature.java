package org.phantazm.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerSpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.server.context.ConfigContext;
import org.phantazm.server.context.DatabaseContext;
import org.phantazm.stats.Databases;
import org.phantazm.stats.general.GeneralDatabase;
import org.phantazm.stats.general.JDBCGeneralDatabase;
import org.phantazm.stats.general.JDBCUsernameDatabase;
import org.phantazm.stats.general.UsernameDatabase;
import org.phantazm.stats.zombies.JDBCZombiesLeaderboardDatabase;
import org.phantazm.stats.zombies.JDBCZombiesStatsDatabase;
import org.phantazm.stats.zombies.ZombiesLeaderboardDatabase;
import org.phantazm.stats.zombies.ZombiesStatsDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DatabaseFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseFeature.class);

    private static UsernameDatabase usernameDatabase;
    private static GeneralDatabase generalDatabase;
    private static ZombiesLeaderboardDatabase zombiesLeaderboardDatabase;
    private static ZombiesStatsDatabase zombiesStatsDatabase;

    private DatabaseFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull DatabaseContext databaseContext, @NotNull ConfigContext configContext) {
        Executor executor = databaseContext.databaseExecutor();
        DataSource dataSource = databaseContext.dataSource();

        usernameDatabase = new JDBCUsernameDatabase(executor, dataSource, Duration.ofDays(30));
        generalDatabase = new JDBCGeneralDatabase(databaseContext.databaseExecutor(), dataSource);
        zombiesLeaderboardDatabase = new JDBCZombiesLeaderboardDatabase(executor, dataSource,
            configContext.zombiesConfig().teamSizes(), configContext.zombiesConfig().trackedModifiers());
        zombiesStatsDatabase = new JDBCZombiesStatsDatabase(executor, dataSource);

        CompletableFuture.allOf(usernameDatabase.initTables(), generalDatabase.initTables(),
            zombiesLeaderboardDatabase.initTables(), zombiesStatsDatabase.initTables()).join();

        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, DatabaseFeature::onPlayerSpawn);

        Databases.init(usernameDatabase);
    }

    private static void onPlayerSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) {
            return;
        }

        generalDatabase.handleJoin(event.getPlayer().getUuid())
            .whenComplete((ignored, throwable) -> {
                if (throwable != null) {
                    LOGGER.warn("Failed to update join times for {}", event.getPlayer().getUuid(), throwable);
                }
            });
    }

    public static @NotNull UsernameDatabase usernameDatabase() {
        return FeatureUtils.check(usernameDatabase);
    }

    public static @NotNull GeneralDatabase generalDatabase() {
        return FeatureUtils.check(generalDatabase);
    }

    public static @NotNull ZombiesLeaderboardDatabase zombiesLeaderboardDatabase() {
        return FeatureUtils.check(zombiesLeaderboardDatabase);
    }

    public static @NotNull ZombiesStatsDatabase zombiesStatsDatabase() {
        return FeatureUtils.check(zombiesStatsDatabase);
    }
}
