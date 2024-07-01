package org.phantazm.stats.general;

import org.jetbrains.annotations.NotNull;
import javax.sql.DataSource;

import org.phantazm.commons.FutureUtils;
import org.phantazm.stats.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JDBCFriendDatabase implements FriendDatabase {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCFriendDatabase.class);

    private final Executor executor;
    private final DataSource dataSource;

    public JDBCFriendDatabase(@NotNull Executor executor, @NotNull DataSource dataSource) {
        this.executor = Objects.requireNonNull(executor);
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Override
    public @NotNull CompletableFuture<Void> initTables() {
        return CompletableFuture.runAsync(() -> {
            DatabaseUtils.runSql(LOGGER, "initTables", dataSource, (connection, statement) -> {
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS friends (
                        p1 UUID NOT NULL,
                        p2 UUID NOT NULL,
                        time_added BIGINT,
                        PRIMARY KEY (p1, p2)
                    );
                    """);
            });
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Set<UUID>> fetchFriends(@NotNull UUID requester, int maxFriends, int friendOffset) {
        return CompletableFuture.supplyAsync(() -> {
            return DatabaseUtils.runPreparedSql(LOGGER, "fetchFriends", dataSource, """
            SELECT p1, p2
            FROM friends
            WHERE ? in (p1, p2)
            LIMIT ? OFFSET ?
            """, (connection, preparedStatement) -> {
                preparedStatement.setString(1, requester.toString());
                preparedStatement.setInt(2, maxFriends);
                preparedStatement.setInt(3, friendOffset);

                ResultSet results = preparedStatement.executeQuery();

                if (!results.next()) {
                    return Set.of();
                }

                Set<UUID> matchingFriends = new HashSet<>(maxFriends);
                do {
                    UUID p1 = UUID.fromString(results.getString(1));
                    UUID p2 = UUID.fromString(results.getString(2));

                    matchingFriends.add(p1.equals(requester)? p2 : p1);
                } while (results.next());

                return Set.copyOf(matchingFriends);
            });
        }, executor);
    }

    // unused - use only for future stat API
    @Override
    public @NotNull CompletableFuture<Set<FriendEntry>> fetchFriendsWithTime(@NotNull UUID requester, int maxFriends, int friendOffset) {
        return CompletableFuture.supplyAsync(() -> {
            return DatabaseUtils.runPreparedSql(LOGGER, "fetchFriendsWithTime", dataSource, """
            SELECT p1, p2, time_added
            FROM friends
            WHERE ? in (p1, p2)
            LIMIT ? OFFSET ?
            """, (connection, preparedStatement) -> {
                preparedStatement.setString(1, requester.toString());
                preparedStatement.setInt(2, maxFriends);
                preparedStatement.setInt(3, friendOffset);

                ResultSet results = preparedStatement.executeQuery();

                if (!results.next()) {
                    return Set.of();
                }

                Set<FriendEntry> matchingFriends = new HashSet<>(maxFriends);
                do {
                    UUID p1 = UUID.fromString(results.getString(1));
                    UUID p2 = UUID.fromString(results.getString(2));
                    long time = results.getLong(3);

                    matchingFriends.add(p1.equals(requester)? new FriendEntry(p2, time) : new FriendEntry(p1, time));
                } while (results.next());

                return Set.copyOf(matchingFriends);
            });
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> isFriend(@NotNull UUID player1, @NotNull UUID player2) {
        if (player1.equals(player2)) {
            return FutureUtils.falseCompletedFuture();
        }

        return CompletableFuture.supplyAsync(() -> {
            return DatabaseUtils.runPreparedSql(LOGGER, "isFriend", dataSource, """
                SELECT COUNT(*)
                FROM friends
                WHERE p1 = ? AND p2 = ?
                """, (connection, preparedStatement) -> {

                if (player1.compareTo(player2) < 0) {
                    preparedStatement.setString(1, player1.toString());
                    preparedStatement.setString(2, player2.toString());
                } else {
                    preparedStatement.setString(2, player1.toString());
                    preparedStatement.setString(1, player2.toString());
                }

                ResultSet result = preparedStatement.executeQuery();

                int foundRows = result.getInt(1);
                return foundRows >= 1;
            });
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> submitFriend(@NotNull UUID player1, @NotNull UUID player2, long timeCreated) {
        if (player1.equals(player2)) {
            return FutureUtils.nullCompletedFuture();
        }

        return CompletableFuture.runAsync(() -> {
            DatabaseUtils.runPreparedSql(LOGGER, "submitFriend", dataSource, """
                INSERT INTO friends (p1, p2, time_added)
                VALUES (?, ?, ?)
                """, (connection, preparedStatement) -> {

                if (player1.compareTo(player2) < 0) {
                    preparedStatement.setString(1, player1.toString());
                    preparedStatement.setString(2, player2.toString());
                } else {
                    preparedStatement.setString(2, player1.toString());
                    preparedStatement.setString(1, player2.toString());
                }
                preparedStatement.setLong(3, timeCreated);

                preparedStatement.execute();
            });
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> deleteFriend(@NotNull UUID player1, @NotNull UUID player2) {
        if (player1.equals(player2)) {
            return FutureUtils.nullCompletedFuture();
        }

        return CompletableFuture.runAsync(() -> {
            DatabaseUtils.runPreparedSql(LOGGER, "deleteFriend", dataSource, """
                DELETE FROM friends
                WHERE p1 = ? AND p2 = ?
                """, (connection, preparedStatement) -> {

                if (player1.compareTo(player2) < 0) {
                    preparedStatement.setString(1, player1.toString());
                    preparedStatement.setString(2, player2.toString());
                } else {
                    preparedStatement.setString(2, player1.toString());
                    preparedStatement.setString(1, player2.toString());
                }

                preparedStatement.execute();
            });
        }, executor);
    }
}
