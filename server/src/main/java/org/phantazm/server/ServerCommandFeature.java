package org.phantazm.server;

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
import org.phantazm.server.permission.DatabasePermissionHandler;
import org.phantazm.server.permission.PermissionHandler;
import org.phantazm.server.role.RoleStore;
import org.phantazm.server.validator.LoginValidator;

import javax.sql.DataSource;
import java.util.concurrent.Executor;

public final class ServerCommandFeature {
    public static final Permission ALL_PERMISSIONS = new Permission("*");

    private static PermissionHandler permissionHandler;

    private ServerCommandFeature() {
    }

    static void initialize(@NotNull LoginValidator validator, boolean whitelist, @NotNull DataSource dataSource,
            @NotNull Executor executor, @NotNull RouterStore store, @NotNull ShutdownConfig shutdownConfig,
            @NotNull ZombiesGamereportConfig zombiesGamereportConfig, @NotNull PlayerViewProvider playerViewProvider,
            @NotNull SceneTransferHelper sceneTransferHelper, @NotNull RoleStore roleStore) {
        ServerCommandFeature.permissionHandler = new DatabasePermissionHandler(dataSource, executor);

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
        manager.register(new GamemodeCommand());
        manager.register(new AddRoleCommand(IdentitySource.MOJANG, roleStore));
        manager.register(new RemoveRoleCommand(IdentitySource.MOJANG, roleStore));

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
