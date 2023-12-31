package org.phantazm.stats.zombies;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public final class Leaderboards {
    private static final Object LOCK = new Object();

    private static LeaderboardDatabase database;

    public static void init(@NotNull LeaderboardDatabase database) {
        Objects.requireNonNull(database);
        synchronized (LOCK) {
            if (Leaderboards.database != null) {
                throw new IllegalStateException("Database has already been defined");
            }

            Leaderboards.database = database;
        }
    }

    public static @NotNull LeaderboardDatabase database() {
        LeaderboardDatabase database = Leaderboards.database;
        if (database == null) {
            throw new IllegalStateException("Database has not been defined!");
        }

        return database;
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    static void executeStatementsInSameTransaction(Logger logger,
        DataSource dataSource, String location, int isolationLevel, String... queries) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            int oldIsolation = connection.getTransactionIsolation();
            connection.setTransactionIsolation(isolationLevel);
            connection.setAutoCommit(false);

            try {
                for (String query : queries) {
                    statement.execute(query);
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
                connection.setTransactionIsolation(oldIsolation);
            }
        } catch (SQLException e) {
            logger.warn("Exception in " + location, e);
        }
    }
}
