package org.phantazm.zombies.stats;

import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
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
    public void synchronize(@NotNull ZombiesPlayerMapStats stats) {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                synchronizeInternal(connection, stats);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Exception while synchronizing Zombies database", throwable);
            }
        });
    }

    protected abstract void synchronizeInternal(@NotNull Connection connection,
            @NotNull ZombiesPlayerMapStats stats) throws SQLException;

    @Override
    public void close() {
        dataSource.close();
    }
}
