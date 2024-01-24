package org.phantazm.server.context;

import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.util.concurrent.Executor;

public record DatabaseContext(@NotNull Executor databaseExecutor,
    @NotNull DataSource dataSource) {
}
