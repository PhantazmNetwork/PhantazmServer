package org.phantazm.server;

import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.RouterStore;
import org.phantazm.core.game.scene.SceneTransferHelper;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.server.command.server.*;
import org.phantazm.server.config.server.ShutdownConfig;
import org.phantazm.server.config.server.ZombiesGamereportConfig;
import org.phantazm.server.permission.FilePermissionHandler;
import org.phantazm.server.permission.PermissionHandler;
import org.phantazm.server.player.LoginValidator;

import java.nio.file.Path;

public final class ServerCommandFeature {
    public static final Path PERMISSIONS_FILE = Path.of("./permissions.yml");
    public static final Permission ALL_PERMISSIONS = new Permission("*");

    private static PermissionHandler permissionHandler;

    private ServerCommandFeature() {
    }

    static void initialize(@NotNull LoginValidator validator, boolean whitelist,
            @NotNull MappingProcessorSource mappingProcessorSource, @NotNull ConfigCodec permissionsCodec,
            @NotNull RouterStore store, @NotNull ShutdownConfig shutdownConfig,
            @NotNull ZombiesGamereportConfig zombiesGamereportConfig, @NotNull PlayerViewProvider playerViewProvider,
            @NotNull SceneTransferHelper sceneTransferHelper) {
        ServerCommandFeature.permissionHandler =
                new FilePermissionHandler(mappingProcessorSource, permissionsCodec, PERMISSIONS_FILE);

        CommandManager manager = MinecraftServer.getCommandManager();
        manager.register(new StopCommand());
        manager.register(new BanCommand(IdentitySource.MOJANG, validator));
        manager.register(new PardonCommand(IdentitySource.MOJANG, validator));
        manager.register(new WhitelistCommand(IdentitySource.MOJANG, validator, whitelist));
        manager.register(new PermissionCommand(permissionHandler, IdentitySource.MOJANG));
        manager.register(new OrderlyShutdownCommand(store, shutdownConfig, MinecraftServer.getGlobalEventHandler()));
        manager.register(new DebugCommand());
        manager.register(new GamereportCommand(store, zombiesGamereportConfig));
        manager.register(new GhostCommand(playerViewProvider, sceneTransferHelper, store));
        manager.register(new FlyCommand());

        manager.getConsoleSender().addPermission(ALL_PERMISSIONS);
    }

    public static @NotNull PermissionHandler permissionHandler() {
        return FeatureUtils.check(permissionHandler);
    }

    public static void flushPermissions() {
        if (permissionHandler != null) {
            permissionHandler.flush();
        }
    }
}
