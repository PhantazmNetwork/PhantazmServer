package org.phantazm.server;

import org.jetbrains.annotations.NotNull;
import org.phantazm.server.player.DatabaseLoginValidator;
import org.phantazm.server.player.LoginValidator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executor;

public final class LoginValidatorFeature {
    private static Connection connection;
    private static LoginValidator loginValidator;

    private LoginValidatorFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull DataSource dataSource, @NotNull Executor executor) {
        try {
            LoginValidatorFeature.connection = dataSource.getConnection();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }

        LoginValidatorFeature.loginValidator = new DatabaseLoginValidator(connection, executor);
    }

    public static @NotNull LoginValidator loginValidator() {
        return FeatureUtils.check(loginValidator);
    }

    public static void end() {
        try {
            if (connection != null) {
                connection.close();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
