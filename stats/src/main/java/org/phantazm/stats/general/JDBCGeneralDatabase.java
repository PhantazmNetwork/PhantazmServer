package org.phantazm.stats.general;

import org.jetbrains.annotations.NotNull;
import org.phantazm.stats.DatabaseUtils;
import org.phantazm.stats.zombies.JDBCZombiesStatsDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JDBCGeneralDatabase implements GeneralDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCZombiesStatsDatabase.class);

    private final Executor executor;

    private final DataSource dataSource;

    public JDBCGeneralDatabase(@NotNull Executor executor, @NotNull DataSource dataSource) {
        this.executor = Objects.requireNonNull(executor);
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Override
    public @NotNull CompletableFuture<Void> initTables() {
        return CompletableFuture.runAsync(() -> {
            DatabaseUtils.runSql(LOGGER, "initTables", dataSource, (connection, statement) -> {
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_stats (
                        player_uuid UUID NOT NULL PRIMARY KEY,
                        first_join BIGINT NOT NULL,
                        last_join BIGINT NOT NULL
                    );
                    """);
            });
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> handleJoin(@NotNull UUID playerUUID) {
        return CompletableFuture.runAsync(() -> {
            DatabaseUtils.runPreparedSql(LOGGER, "handleJoin", dataSource, """
                INSERT INTO player_stats (player_uuid, first_join, last_join)
                VALUES(?, UNIX_TIMESTAMP(), UNIX_TIMESTAMP())
                ON DUPLICATE KEY UPDATE last_join=UNIX_TIMESTAMP()
                """, (connection, statement) -> {
                statement.setString(1, playerUUID.toString());
                statement.execute();
            });
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> updateJoin(@NotNull UUID playerUUID, long firstJoin, long lastJoin) {
        return CompletableFuture.runAsync(() -> {
            DatabaseUtils.runPreparedSql(LOGGER, "handleJoin", dataSource, """
                INSERT INTO player_stats (player_uuid, first_join, last_join)
                VALUES(?, ?, ?)
                ON DUPLICATE KEY UPDATE 
                first_join=?,
                last_join=?
                """, (connection, statement) -> {
                statement.setString(1, playerUUID.toString());
                statement.setLong(2, firstJoin);
                statement.setLong(3, lastJoin);

                statement.setLong(4, firstJoin);
                statement.setLong(5, lastJoin);

                statement.execute();
            });
        }, executor);
    }
}