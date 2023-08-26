package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.bridge.Configuration;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.FileUtils;
import org.phantazm.server.role.BasicRoleStore;
import org.phantazm.server.role.RoleCreator;
import org.phantazm.server.role.RoleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;

public final class RoleFeature {
    public static final Path ROLE_FOLDER = Path.of("./roles");
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleFeature.class);
    private static RoleStore roleStore;

    private RoleFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull DataSource dataSource, @NotNull Executor executor, @NotNull ConfigCodec codec,
        @NotNull ContextManager contextManager) {
        roleStore = new BasicRoleStore(dataSource, executor);

        try {
            FileUtils.createDirectories(ROLE_FOLDER);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(ROLE_FOLDER, "*.yml")) {
                for (Path path : stream) {
                    loadFile(path, codec, contextManager);
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

        ElementContext context = contextManager.makeContext(element.asNode());
        RoleCreator roleCreator = context.provide();
        roleStore.register(roleCreator.get());
    }

    public static @NotNull RoleStore roleStore() {
        return FeatureUtils.check(roleStore);
    }
}
