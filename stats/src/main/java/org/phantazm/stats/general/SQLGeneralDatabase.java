package org.phantazm.stats.general;

import org.jetbrains.annotations.NotNull;
import org.phantazm.stats.zombies.SQLZombiesDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SQLGeneralDatabase implements GeneralDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLZombiesDatabase.class);

    private final Executor executor;

    private final DataSource dataSource;

    private final JooqGeneralSQLFetcher sqlFetcher;

    public SQLGeneralDatabase(@NotNull Executor executor, @NotNull DataSource dataSource,
            @NotNull JooqGeneralSQLFetcher sqlFetcher) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
        this.sqlFetcher = Objects.requireNonNull(sqlFetcher, "sqlFetcher");
    }

    @Override
    public @NotNull CompletableFuture<Void> handleJoin(@NotNull UUID playerUUID, @NotNull ZonedDateTime time) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                sqlFetcher.handleJoin(connection, playerUUID, time);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor).whenComplete(this::logException);
    }

    private <T> void logException(T ignored, Throwable throwable) {
        if (throwable != null) {
            LOGGER.warn("Exception while querying database", throwable);
        }
    }
}
