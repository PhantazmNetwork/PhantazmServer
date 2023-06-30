package org.phantazm.stats.zombies;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SQLZombiesDatabase implements ZombiesDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLZombiesDatabase.class);

    private final Executor executor;

    private final DataSource dataSource;

    private final ZombiesSQLFetcher sqlFetcher;

    public SQLZombiesDatabase(@NotNull Executor executor, @NotNull DataSource dataSource,
            @NotNull ZombiesSQLFetcher sqlFetcher) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
        this.sqlFetcher = Objects.requireNonNull(sqlFetcher, "sqlFetcher");
    }

    @Override
    public @NotNull CompletableFuture<Void> synchronizeZombiesPlayerMapStats(@NotNull ZombiesPlayerMapStats stats) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                sqlFetcher.synchronizeZombiesPlayerMapStats(connection, stats);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor).whenComplete(this::logException);
    }

    @Override
    public @NotNull CompletableFuture<ZombiesPlayerMapStats> getMapStats(@NotNull UUID playerUUID,
            @NotNull Key mapKey) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                return sqlFetcher.getMapStats(connection, playerUUID, mapKey);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor).whenComplete(this::logException);
    }

    @Override
    public @NotNull CompletableFuture<List<BestTime>> getBestTimes(@NotNull Key mapKey) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                return sqlFetcher.getBestTimes(connection, mapKey);
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
