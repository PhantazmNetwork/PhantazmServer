package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.bridge.Configuration;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.FileUtils;
import org.phantazm.core.role.JDBCRoleStore;
import org.phantazm.core.role.RoleCreator;
import org.phantazm.core.role.RoleStore;
import org.phantazm.server.context.DatabaseContext;
import org.phantazm.server.context.DataLoadingContext;
import org.phantazm.server.context.EthyleneContext;
import org.phantazm.server.permission.JDBCPermissionHandler;
import org.phantazm.server.permission.PermissionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class PermissionFeature {
    public static final Path ROLE_FOLDER = Path.of("./roles");
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionFeature.class);

    private static RoleStore roleStore;
    private static PermissionHandler permissionHandler;

    private PermissionFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull DatabaseContext databaseContext, @NotNull EthyleneContext ethyleneContext,
        @NotNull DataLoadingContext dataLoadingContext) {
        roleStore = new JDBCRoleStore(databaseContext.dataSource(), databaseContext.databaseExecutor());
        permissionHandler = new JDBCPermissionHandler(databaseContext.dataSource(), databaseContext.databaseExecutor(),
            roleStore);

        CompletableFuture.allOf(roleStore.initTables(), permissionHandler.initTables()).join();

        try {
            FileUtils.createDirectories(ROLE_FOLDER);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(ROLE_FOLDER, "*.yml")) {
                for (Path path : stream) {
                    loadFile(path, ethyleneContext.yamlCodec(), dataLoadingContext.contextManager());
                }
            }
        } catch (IOException e) {
            LOGGER.warn("IOException when loading roles", e);
        }
    }

    private static void loadFile(Path path, ConfigCodec codec, ContextManager contextManager) throws IOException {
        ConfigElement element = Configuration.read(path, codec);
        if (!element.isNode()) {
            throw new IOException("Invalid element; expected node");
        }

        RoleCreator roleCreator = contextManager.makeContext(element.asNode()).provide();
        roleStore.register(roleCreator.get());
    }

    public static @NotNull RoleStore roleStore() {
        return FeatureUtils.check(roleStore);
    }

    public static @NotNull PermissionHandler permissionHandler() {
        return FeatureUtils.check(permissionHandler);
    }
}
