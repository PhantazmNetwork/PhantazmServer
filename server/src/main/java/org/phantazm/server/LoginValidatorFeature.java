package org.phantazm.server;

import org.jetbrains.annotations.NotNull;
import org.phantazm.server.validator.JDBCLoginValidator;
import org.phantazm.server.validator.LoginValidator;

import javax.sql.DataSource;
import java.util.concurrent.Executor;

public final class LoginValidatorFeature {
    private static LoginValidator loginValidator;

    private LoginValidatorFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull DataSource dataSource, @NotNull Executor executor) {
        LoginValidatorFeature.loginValidator = new JDBCLoginValidator(dataSource, executor);
        LoginValidatorFeature.loginValidator.initTables();
    }

    public static @NotNull LoginValidator loginValidator() {
        return FeatureUtils.check(loginValidator);
    }
}
