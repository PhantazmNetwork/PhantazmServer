package org.phantazm.stats.zombies;

import com.zaxxer.hikari.HikariDataSource;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class HikariZombiesDatabase implements Closeable, ZombiesDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(HikariZombiesDatabase.class);

    private final Executor executor;

    private final HikariDataSource dataSource;

    private final ZombiesSQLFetcher sqlFetcher;

    public HikariZombiesDatabase(@NotNull Executor executor, @NotNull HikariDataSource dataSource,
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
    public CompletableFuture<List<BestTime>> getBestTimes(@NotNull Key mapKey) {
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

    @Override
    public void close() {
        dataSource.close();
    }
}
