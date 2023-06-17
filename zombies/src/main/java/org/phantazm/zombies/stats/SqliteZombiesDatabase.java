package org.phantazm.zombies.stats;

import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Executor;

public class SqliteZombiesDatabase extends HikariZombiesDatabase {
    public SqliteZombiesDatabase(@NotNull Executor executor, @NotNull HikariDataSource dataSource) {
        super(executor, dataSource);
    }

    @Override
    protected void synchronizeInternal(@NotNull Connection connection, @NotNull ZombiesPlayerMapStats stats)
            throws SQLException {
        PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO zombies_player_map_stats (player_uuid, map_key, games_played, wins, rounds_survived, kills, knocks, deaths, revives, regular_shots, headshots)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (player_uuid, map_key) DO UPDATE
                SET games_played = games_played + ?,
                wins = wins + ?,
                rounds_survived = rounds_survived + ?,
                kills = kills + ?,
                knocks = knocks + ?,
                deaths = deaths + ?,
                revives = revives + ?,
                regular_shots = regular_shots + ?,
                headshots = headshots = ?
                """);
        statement.setString(1, stats.getPlayerUUID().toString());
        statement.setString(2, stats.getMapKey().asString());
        statement.setInt(3, stats.getGamesPlayed());
        statement.setInt(4, stats.getWins());
        statement.setInt(5, stats.getRoundsSurvived());
        statement.setInt(6, stats.getKills());
        statement.setInt(7, stats.getKnocks());
        statement.setInt(8, stats.getKnocks());
        statement.setInt(9, stats.getRevives());
        statement.setInt(10, stats.getRegularShots());
        statement.setInt(11, stats.getHeadshots());
        statement.setInt(12, stats.getGamesPlayed());
        statement.setInt(13, stats.getWins());
        statement.setInt(14, stats.getRoundsSurvived());
        statement.setInt(15, stats.getKills());
        statement.setInt(16, stats.getKnocks());
        statement.setInt(17, stats.getKnocks());
        statement.setInt(18, stats.getRevives());
        statement.setInt(19, stats.getRegularShots());
        statement.setInt(20, stats.getHeadshots());
        statement.execute();
    }
}
