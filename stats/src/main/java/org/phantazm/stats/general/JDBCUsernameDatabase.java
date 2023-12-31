package org.phantazm.stats.general;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
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

    public JDBCUsernameDatabase(@NotNull Executor executor, @NotNull DataSource dataSource, @NotNull Duration expireTime) {
        this.executor = Objects.requireNonNull(executor);
        this.dataSource = Objects.requireNonNull(dataSource);
        this.expireTime = expireTime;
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
            Utils.runSql(LOGGER, "initTables", dataSource, (connection, statement) -> {
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS username_cache (
                        uuid UUID NOT NULL PRIMARY KEY,
                        username VARCHAR(16) NOT NULL,
                        last_updated BIGINT NOT NULL
                    );
                    """);
            });
        }, executor);
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @Override
    public @NotNull CompletableFuture<Optional<String>> cachedUsername(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            return Utils.runSql(LOGGER, "cachedUsername", dataSource, (connection, statement) -> {
                String uuidString = uuid.toString();
                ResultSet result = statement.executeQuery("""
                    SELECT username, last_updated FROM username_cache
                    WHERE uuid = '%1s'
                    """.formatted(uuidString));
                if (!result.next()) {
                    return Optional.empty();
                }

                long elapsed = System.currentTimeMillis() - result.getLong(2);
                if (elapsed > expireTime.toMillis()) {
                    statement.execute("""
                        REMOVE FROM username_cache
                        WHERE uuid = '%1s';
                        """.formatted(uuidString));
                    return Optional.empty();
                }

                return Optional.of(result.getString(1));
            });
        }, executor);
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @Override
    public @NotNull CompletableFuture<Void> submitUsername(@NotNull UUID uuid, @NotNull String username) {
        String filteredUsername = filterUsername(username);

        return CompletableFuture.runAsync(() -> {
            Utils.runSql(LOGGER, "submitUsername", dataSource, (connection, statement) -> {
                statement.execute("""
                    REPLACE INTO username_cache (username, last_updated)
                    VALUES (%1s, %2s)
                    """.formatted(uuid, filteredUsername));
            });
        }, executor);
    }
}
