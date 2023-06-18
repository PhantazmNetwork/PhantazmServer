package org.phantazm.zombies.stats;

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

public abstract class HikariZombiesDatabase implements Closeable, ZombiesDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(HikariZombiesDatabase.class);

    private final Executor executor;

    private final HikariDataSource dataSource;

    public HikariZombiesDatabase(@NotNull Executor executor, @NotNull HikariDataSource dataSource) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    @Override
    public @NotNull CompletableFuture<Void> synchronizeZombiesPlayerMapStats(@NotNull ZombiesPlayerMapStats stats) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                synchronizeZombiesPlayerMapStatsInternal(connection, stats);
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
                return getBestTimesInternal(connection, mapKey);
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

    protected abstract void synchronizeZombiesPlayerMapStatsInternal(@NotNull Connection connection,
            @NotNull ZombiesPlayerMapStats stats) throws SQLException;

    protected abstract @NotNull List<BestTime> getBestTimesInternal(@NotNull Connection connection, @NotNull Key mapKey)
            throws SQLException;

    @Override
    public void close() {
        dataSource.close();
    }
}
