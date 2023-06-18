package org.phantazm.zombies.stats;

import com.zaxxer.hikari.HikariDataSource;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

public class MariaDBZombiesDatabase extends HikariZombiesDatabase {
    public MariaDBZombiesDatabase(@NotNull Executor executor, @NotNull HikariDataSource dataSource) {
        super(executor, dataSource);
    }

    @Override
    protected void synchronizeZombiesPlayerMapStatsInternal(@NotNull Connection connection, @NotNull ZombiesPlayerMapStats stats) throws
                                                                                                             SQLException {
        PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO zombies_player_map_stats (player_uuid, map_key, games_played, wins, best_time, rounds_survived, kills, knocks, deaths, revives, regular_shots, headshots)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                games_played = games_played + ?,
                wins = wins + ?,
                best_time = CASE WHEN ? IS NULL THEN best_time ELSE ? END,
                rounds_survived = rounds_survived + ?,
                kills = kills + ?,
                knocks = knocks + ?,
                deaths = deaths + ?,
                revives = revives + ?,
                regular_shots = regular_shots + ?,
                headshots = headshots + ?
                """);
        int parameterIndex = 0;
        statement.setString(++parameterIndex, stats.getPlayerUUID().toString());
        statement.setString(++parameterIndex, stats.getMapKey().asString());
        statement.setInt(++parameterIndex, stats.getGamesPlayed());
        statement.setInt(++parameterIndex, stats.getWins());
        if (stats.getBestTime().isPresent()) {
            statement.setLong(++parameterIndex, stats.getBestTime().get());
        } else {
            statement.setNull(++parameterIndex, Types.INTEGER);
        }
        statement.setInt(++parameterIndex, stats.getRoundsSurvived());
        statement.setInt(++parameterIndex, stats.getKills());
        statement.setInt(++parameterIndex, stats.getKnocks());
        statement.setInt(++parameterIndex, stats.getKnocks());
        statement.setInt(++parameterIndex, stats.getRevives());
        statement.setInt(++parameterIndex, stats.getRegularShots());
        statement.setInt(++parameterIndex, stats.getHeadshots());
        statement.setInt(++parameterIndex, stats.getGamesPlayed());
        statement.setInt(++parameterIndex, stats.getWins());
        if (stats.getBestTime().isPresent()) {
            statement.setLong(++parameterIndex, stats.getBestTime().get());
            statement.setLong(++parameterIndex, stats.getBestTime().get());
        } else {
            statement.setNull(++parameterIndex, Types.INTEGER);
            statement.setNull(++parameterIndex, Types.INTEGER);
        }
        statement.setInt(++parameterIndex, stats.getRoundsSurvived());
        statement.setInt(++parameterIndex, stats.getKills());
        statement.setInt(++parameterIndex, stats.getKnocks());
        statement.setInt(++parameterIndex, stats.getKnocks());
        statement.setInt(++parameterIndex, stats.getRevives());
        statement.setInt(++parameterIndex, stats.getRegularShots());
        statement.setInt(++parameterIndex, stats.getHeadshots());
        statement.execute();
    }

    @Override
    protected @NotNull List<BestTime> getBestTimesInternal(@NotNull Connection connection, @NotNull Key mapKey)
            throws SQLException {
        PreparedStatement statement = connection.prepareStatement("""
                SELECT player_uuid, best_time
                FROM zombies_player_map_stats
                WHERE map_key = ?
                AND best_time IS NOT NULL
                ORDER BY best_time
                """);
        statement.setString(1, mapKey.asString());
        ResultSet resultSet = statement.executeQuery();
        List<BestTime> bestTimes = new ArrayList<>();
        while (resultSet.next()) {
            UUID uuid = UUID.fromString(resultSet.getString("player_uuid"));
            long bestTime = resultSet.getLong("best_time");
            bestTimes.add(new BestTime(uuid, bestTime));
        }

        return bestTimes;
    }
}
