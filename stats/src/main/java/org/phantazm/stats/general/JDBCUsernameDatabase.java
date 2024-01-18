package org.phantazm.stats.general;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.FutureUtils;
import org.phantazm.stats.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JDBCUsernameDatabase implements UsernameDatabase {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCUsernameDatabase.class);

    private final Executor executor;
    private final DataSource dataSource;
    private final Duration expireTime;

    private final Cache<UUID, String> uuidToUsername;
    private final Cache<String, UUID> usernameToUuid;

    public JDBCUsernameDatabase(@NotNull Executor executor, @NotNull DataSource dataSource, @NotNull Duration expireTime) {
        this.executor = Objects.requireNonNull(executor);
        this.dataSource = Objects.requireNonNull(dataSource);
        this.expireTime = expireTime;

        Caffeine<Object, Object> builder = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(10));
        this.uuidToUsername = builder.build();
        this.usernameToUuid = builder.build();
    }

    private static boolean isValidChar(char c) {
        return c == '_' || (c <= 'z' && c >= 'a') || (c <= 'Z' && c >= 'A') || (c <= '9' && c >= '0');
    }

    private static String filterUsername(String string) {
        int truncatedLength = Math.min(string.length(), 16);
        StringBuilder builder = new StringBuilder(truncatedLength);
        string = string.substring(0, truncatedLength);

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (isValidChar(c)) {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    @Override
    public @NotNull CompletableFuture<Void> initTables() {
        return CompletableFuture.runAsync(() -> {
            DatabaseUtils.runSql(LOGGER, "initTables", dataSource, (connection, statement) -> {
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS username_cache (
                        uuid UUID NOT NULL PRIMARY KEY,
                        username VARCHAR(16) NOT NULL,
                        last_updated BIGINT NOT NULL
                    );
                    """);

                try (PreparedStatement preparedStatement = connection.prepareStatement("""
                    CREATE OR REPLACE EVENT clean_expired_entries
                    ON SCHEDULE EVERY 12 HOUR
                    DO DELETE FROM phantazm_db.username_cache
                    WHERE (SELECT UNIX_TIMESTAMP()) - last_updated > ?
                    """)) {
                    preparedStatement.setLong(1, expireTime.toSeconds());
                    preparedStatement.execute();
                }
            });
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Optional<String>> cachedUsername(@NotNull UUID uuid) {
        String username = uuidToUsername.getIfPresent(uuid);
        if (username != null) {
            return FutureUtils.completedFuture(Optional.of(username));
        }

        return CompletableFuture.supplyAsync(() -> {
            String uuidString = uuid.toString();

            return DatabaseUtils.runPreparedSql(LOGGER, "cachedUsername", dataSource, """
                SELECT username, last_updated FROM phantazm_db.username_cache
                WHERE uuid = ?
                """, (connection, statement) -> {

                statement.setString(1, uuidString);
                ResultSet result = statement.executeQuery();

                if (!result.next()) {
                    return Optional.empty();
                }

                String name = result.getString(1);
                usernameToUuid.put(name, uuid);
                uuidToUsername.put(uuid, name);
                return Optional.of(name);
            });
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Optional<UUID>> cachedUUID(@NotNull String username) {
        String filteredName = filterUsername(username);
        UUID uuid = usernameToUuid.getIfPresent(filteredName);
        if (uuid != null) {
            return FutureUtils.completedFuture(Optional.of(uuid));
        }

        return CompletableFuture.supplyAsync(() -> {
            return DatabaseUtils.runPreparedSql(LOGGER, "cachedUUID", dataSource, """
                SELECT uuid, last_updated FROM phantazm_db.username_cache
                WHERE username = ?
                ORDER BY last_updated DESC
                LIMIT 1
                """, (connection, statement) -> {
                statement.setString(1, filteredName);

                ResultSet result = statement.executeQuery();
                if (!result.next()) {
                    return Optional.empty();
                }

                UUID newUuid = UUID.fromString(result.getString(1));
                usernameToUuid.put(filteredName, newUuid);
                uuidToUsername.put(newUuid, filteredName);
                return Optional.of(newUuid);
            });
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> submitUsername(@NotNull UUID uuid, @NotNull String username) {
        return CompletableFuture.runAsync(() -> {
            String filteredUsername = filterUsername(username);

            uuidToUsername.put(uuid, filteredUsername);
            usernameToUuid.put(filteredUsername, uuid);

            DatabaseUtils.runPreparedSql(LOGGER, "submitUsername", dataSource, """
                REPLACE INTO phantazm_db.username_cache (uuid, username, last_updated)
                VALUES (?, ?, UNIX_TIMESTAMP())
                """, (connection, statement) -> {

                statement.setString(1, uuid.toString());
                statement.setString(2, filteredUsername);

                statement.executeUpdate();
            });
        }, executor);
    }
}
