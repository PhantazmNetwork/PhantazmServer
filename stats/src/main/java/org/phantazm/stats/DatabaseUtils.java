package org.phantazm.stats;

import com.github.steanky.toolkit.function.ThrowingFunction;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.phantazm.stats.general.ThrowingBiConsumer;
import org.phantazm.stats.general.ThrowingBiFunction;
import org.phantazm.stats.general.ThrowingConsumer;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

@SuppressWarnings("SqlSourceToSinkFlow")
public final class DatabaseUtils {
    public static void runSql(@NotNull Logger logger, @NotNull String location, @NotNull DataSource dataSource,
        @NotNull ThrowingBiConsumer<? super Connection, ? super Statement, ? extends SQLException> function) {
        runSql(logger, location, () -> null, dataSource, (connection, statement) -> {
            function.accept(connection, statement);
            return null;
        });
    }

    public static void runSql(@NotNull Logger logger, @NotNull String location, @NotNull DataSource dataSource,
        @NotNull ThrowingConsumer<? super Connection, ? extends SQLException> function) {
        runSql(logger, location, () -> null, dataSource, (connection, statement) -> {
            function.accept(connection);
            return null;
        });
    }

    public static void runPreparedSql(@NotNull Logger logger, @NotNull String location, @NotNull DataSource dataSource,
        @NotNull @Language("sql") String sql,
        @NotNull ThrowingBiConsumer<? super Connection, ? super PreparedStatement, ? extends SQLException> function) {
        runPreparedSql(logger, location, () -> null, dataSource, sql, (connection, statement) -> {
            function.accept(connection, statement);
            return null;
        });
    }

    public static <R> R runSql(@NotNull Logger logger, @NotNull String location, @NotNull DataSource dataSource,
        @NotNull ThrowingBiFunction<? super Connection, ? super Statement, ? extends R, ? extends SQLException> function) {
        return runSql(logger, location, () -> null, dataSource, function);
    }

    public static <R> R runSql(@NotNull Logger logger, @NotNull String location, @NotNull DataSource dataSource,
        @NotNull ThrowingFunction<? super Connection, ? extends R, ? extends SQLException> function) {
        return runSql(logger, location, () -> null, dataSource, function);
    }

    public static <R> R runPreparedSql(@NotNull Logger logger, @NotNull String location, @NotNull DataSource dataSource,
        @NotNull @Language("sql") String sql,
        @NotNull ThrowingBiFunction<? super Connection, ? super PreparedStatement, ? extends R, ? extends SQLException> function) {
        return runPreparedSql(logger, location, () -> null, dataSource, sql, function);
    }

    public static <R> R runSql(@NotNull Logger logger, @NotNull String location, @NotNull Supplier<? extends R> def, @NotNull DataSource dataSource,
        @NotNull ThrowingBiFunction<? super Connection, ? super Statement, ? extends R, ? extends SQLException> function) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            return function.apply(connection, statement);
        } catch (SQLException e) {
            logger.warn("Exception running SQL in " + location, e);
        }

        return def.get();
    }

    public static <R> R runSql(@NotNull Logger logger, @NotNull String location, @NotNull Supplier<? extends R> def, @NotNull DataSource dataSource,
        @NotNull ThrowingFunction<? super Connection, ? extends R, ? extends SQLException> function) {
        try (Connection connection = dataSource.getConnection()) {
            return function.apply(connection);
        } catch (SQLException e) {
            logger.warn("Exception running SQL in " + location, e);
        }

        return def.get();
    }

    public static <R> R runPreparedSql(@NotNull Logger logger, @NotNull String location, @NotNull Supplier<? extends R> def,
        @NotNull DataSource dataSource, @NotNull @Language("sql") String sql,
        @NotNull ThrowingBiFunction<? super Connection, ? super PreparedStatement, ? extends R, ? extends SQLException> function) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return function.apply(connection, preparedStatement);
        } catch (SQLException e) {
            logger.warn("Exception running SQL in " + location, e);
        }

        return def.get();
    }
}