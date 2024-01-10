package org.phantazm.stats.zombies;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.stats.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JDBCZombiesStatsDatabase implements ZombiesStatsDatabase {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCZombiesStatsDatabase.class);

    private final Executor executor;

    private final DataSource dataSource;

    public JDBCZombiesStatsDatabase(@NotNull Executor executor, @NotNull DataSource dataSource) {
        this.executor = Objects.requireNonNull(executor);
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Override
    public @NotNull CompletableFuture<Void> initTables() {
        return CompletableFuture.runAsync(() -> {
            DatabaseUtils.runSql(LOGGER, "initTables", dataSource, (connection, statement) -> {
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS zombies_player_map_stats (
                        player_uuid UUID NOT NULL,
                        map_key VARCHAR(64) NOT NULL,
                        games_played INT NOT NULL,
                        wins INT NOT NULL,
                        best_round INT NOT NULL,
                        rounds_survived INT NOT NULL,
                        kills INT NOT NULL,
                        coins_gained BIGINT NOT NULL,
                        coins_spent BIGINT NOT NULL,
                        knocks INT NOT NULL,
                        deaths INT NOT NULL,
                        revives INT NOT NULL,
                        shots BIGINT NOT NULL,
                        regular_hits BIGINT NOT NULL,
                        headshot_hits BIGINT NOT NULL,
                        
                        PRIMARY KEY (player_uuid, map_key)
                    );
                    """);
            });
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> synchronizeZombiesPlayerMapStats(@NotNull ZombiesPlayerMapStats stats) {
        return CompletableFuture.runAsync(() -> {
            DatabaseUtils.runPreparedSql(LOGGER, "synchronizeZombiesPlayerMapStats", dataSource, """
                INSERT INTO zombies_player_map_stats (player_uuid, map_key, games_played, wins, best_round,
                rounds_survived, kills, coins_gained, coins_spent, knocks, deaths, revives, shots, regular_hits,
                headshot_hits)
                VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    games_played=games_played+?,
                    wins=wins+?,
                    best_round=MAX(best_round, 0),
                    rounds_survived=rounds_survived+?,
                    kills=kills+?,
                    coins_gained=coins_gained+?,
                    coins_spent=coins_spent+?,
                    knocks=knocks+?,
                    deaths=deaths+?,
                    revives=revives+?,
                    shots=shots+?,
                    regular_hits=regular_hits+?,
                    headshot_hits=headshot_hits+?
                """, (connection, statement) -> {
                statement.setString(1, stats.getPlayerUUID().toString());
                statement.setString(2, stats.getMapKey().asString());
                statement.setInt(3, stats.getGamesPlayed());
                statement.setInt(4, stats.getWins());
                statement.setInt(5, stats.getBestRound());
                statement.setInt(6, stats.getRoundsSurvived());
                statement.setInt(7, stats.getKills());
                statement.setLong(8, stats.getCoinsGained());
                statement.setLong(9, stats.getCoinsSpent());
                statement.setInt(10, stats.getKnocks());
                statement.setInt(11, stats.getDeaths());
                statement.setInt(12, stats.getRevives());
                statement.setLong(13, stats.getShots());
                statement.setLong(14, stats.getRegularHits());
                statement.setLong(15, stats.getHeadshotHits());

                statement.setInt(16, stats.getGamesPlayed());
                statement.setInt(17, stats.getWins());
                statement.setInt(18, stats.getBestRound());
                statement.setInt(19, stats.getRoundsSurvived());
                statement.setInt(20, stats.getKills());
                statement.setLong(21, stats.getCoinsGained());
                statement.setLong(22, stats.getCoinsSpent());
                statement.setInt(23, stats.getKnocks());
                statement.setInt(24, stats.getDeaths());
                statement.setInt(25, stats.getRevives());
                statement.setLong(26, stats.getShots());
                statement.setLong(27, stats.getRegularHits());
                statement.setLong(28, stats.getHeadshotHits());

                statement.execute();
            });
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<ZombiesPlayerMapStats> getMapStats(@NotNull UUID playerUUID,
        @NotNull Key mapKey) {
        return CompletableFuture.supplyAsync(() -> {
            return DatabaseUtils.runPreparedSql(LOGGER, "getMapStats",
                () -> BasicZombiesPlayerMapStats.createBasicStats(playerUUID, mapKey), dataSource, """
                    SELECT games_played, wins, best_round, rounds_survived, kills, coins_gained, coins_spent, knocks,
                    deaths, revives, shots, regular_hits, headshot_hits FROM zombies_player_map_stats
                    WHERE (player_uuid, map_key) = (?, ?)
                    LIMIT 1
                    """, (connection, statement) -> {
                    statement.setString(1, playerUUID.toString());
                    statement.setString(2, mapKey.asString());
                    statement.setFetchSize(1);

                    ResultSet result = statement.executeQuery();
                    if (!result.next()) {
                        return BasicZombiesPlayerMapStats.createBasicStats(playerUUID, mapKey);
                    }

                    return new BasicZombiesPlayerMapStats(playerUUID, mapKey,
                        result.getInt(1), result.getInt(2), result.getInt(3),
                        result.getInt(4), result.getInt(5), result.getLong(6),
                        result.getLong(7), result.getInt(8), result.getInt(9),
                        result.getInt(10), result.getLong(11), result.getLong(12),
                        result.getLong(13));
                });
        }, executor);
    }

}
