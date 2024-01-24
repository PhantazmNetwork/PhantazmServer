package org.phantazm.server;

import org.jetbrains.annotations.NotNull;
import org.phantazm.server.context.DatabaseContext;
import org.phantazm.server.validator.JDBCLoginValidator;
import org.phantazm.server.validator.LoginValidator;

public final class LoginValidatorFeature {
    private static LoginValidator loginValidator;

    private LoginValidatorFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull DatabaseContext databaseContext) {
        LoginValidatorFeature.loginValidator = new JDBCLoginValidator(databaseContext.dataSource(),
            databaseContext.databaseExecutor());

        LoginValidatorFeature.loginValidator.initTables().join();
    }

    public static @NotNull LoginValidator loginValidator() {
        return FeatureUtils.check(loginValidator);
    }
}
