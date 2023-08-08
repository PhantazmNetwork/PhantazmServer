package org.phantazm.stats.general;

import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.impl.SQLDataType;
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
import java.util.function.Consumer;

import static org.jooq.impl.DSL.*;

public class SQLGeneralDatabase implements GeneralDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLZombiesDatabase.class);

    private final Executor executor;

    private final DataSource dataSource;

    public SQLGeneralDatabase(@NotNull Executor executor, @NotNull DataSource dataSource) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    @Override
    public @NotNull CompletableFuture<Void> handleJoin(@NotNull UUID playerUUID, @NotNull ZonedDateTime time) {
        return executeSQL(connection -> {
            long timestamp = time.toEpochSecond();
            DSLContext context = using(connection);
            context.insertInto(table("phantazm_player_stats"), field("player_uuid"), field("first_join"))
                    .values(playerUUID.toString(), timestamp).onDuplicateKeyUpdate().set(field("first_join"),
                            when(field("first_join").isNull(), timestamp).otherwise(field("first_join", SQLDataType.BIGINT)))
                    .execute();

            context.update(table("phantazm_player_stats")).set(field("last_join"), timestamp)
                    .where(field("player_uuid").eq(playerUUID)).execute();
        });
    }

    private CompletableFuture<Void> executeSQL(Consumer<Connection> consumer) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                consumer.accept(connection);
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
