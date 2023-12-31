package org.phantazm.stats.general;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class Utils {
    public static void runSql(@NotNull Logger logger, @NotNull String location, @NotNull DataSource dataSource,
        ThrowingBiConsumer<? super Connection, ? super Statement, ? extends SQLException> function) {
        runSql(logger, location, null, dataSource, (connection, statement) -> {
            function.accept(connection, statement);
            return null;
        });
    }

    public static <R> R runSql(@NotNull Logger logger, @NotNull String location, @NotNull DataSource dataSource,
        ThrowingBiFunction<? super Connection, ? super Statement, ? extends R, ? extends SQLException> function) {
        return runSql(logger, location, null, dataSource, function);
    }

    public static <R> R runSql(@NotNull Logger logger, @NotNull String location, R def, @NotNull DataSource dataSource,
        ThrowingBiFunction<? super Connection, ? super Statement, ? extends R, ? extends SQLException> function) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            return function.apply(connection, statement);
        } catch (SQLException e) {
            logger.warn("Exception running SQL query in " + location);
        }

        return def;
    }
}
