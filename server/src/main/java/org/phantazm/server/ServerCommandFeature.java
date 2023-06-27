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

public final class ServerCommandFeature {
    public static final Permission ALL_PERMISSIONS = new Permission("*");

    private static PermissionHandler permissionHandler;

    private ServerCommandFeature() {
    }

    static void initialize(@NotNull CommandManager commandManager, @NotNull LoginValidator loginValidator,
            boolean whitelist, @NotNull MappingProcessorSource mappingProcessorSource,
            @NotNull ConfigCodec permissionsCodec, @NotNull RouterStore routerStore,
            @NotNull ShutdownConfig shutdownConfig, @NotNull ZombiesGamereportConfig zombiesGamereportConfig,
            @NotNull PlayerViewProvider playerViewProvider, @NotNull SceneTransferHelper sceneTransferHelper) {
        ServerCommandFeature.permissionHandler =
                new FilePermissionHandler(mappingProcessorSource, permissionsCodec, PhantazmServer.PERMISSIONS_FILE);

        commandManager.register(new StopCommand());
        commandManager.register(new BanCommand(IdentitySource.MOJANG, loginValidator));
        commandManager.register(new PardonCommand(IdentitySource.MOJANG, loginValidator));
        commandManager.register(new WhitelistCommand(IdentitySource.MOJANG, loginValidator, whitelist));
        commandManager.register(new PermissionCommand(permissionHandler, IdentitySource.MOJANG));
        commandManager.register(
                new OrderlyShutdownCommand(routerStore, shutdownConfig, MinecraftServer.getGlobalEventHandler()));
        commandManager.register(new DebugCommand());
        commandManager.register(new GamereportCommand(routerStore, zombiesGamereportConfig));
        commandManager.register(new GhostCommand(playerViewProvider, sceneTransferHelper, routerStore));

        commandManager.getConsoleSender().addPermission(ALL_PERMISSIONS);
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
