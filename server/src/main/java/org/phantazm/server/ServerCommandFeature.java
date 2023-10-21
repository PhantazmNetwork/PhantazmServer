package org.phantazm.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
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
        @NotNull Executor executor, @NotNull ShutdownConfig shutdownConfig,
        @NotNull ZombiesGamereportConfig zombiesGamereportConfig, @NotNull PlayerViewProvider playerViewProvider,
        @NotNull RoleStore roleStore) {
        ServerCommandFeature.permissionHandler = new DatabasePermissionHandler(dataSource, executor, roleStore);

        CommandManager manager = MinecraftServer.getCommandManager();
        manager.register(new StopCommand());
        manager.register(new BanCommand(IdentitySource.MOJANG, validator));
        manager.register(new BanHistoryCommand(IdentitySource.MOJANG, validator));
        manager.register(new BanHistoryClearCommand(IdentitySource.MOJANG, validator));
        manager.register(new PardonCommand(IdentitySource.MOJANG, validator));
        manager.register(new WhitelistCommand(IdentitySource.MOJANG, validator, whitelist));
        manager.register(new PermissionCommand(permissionHandler, IdentitySource.MOJANG));
        manager.register(new OrderlyShutdownCommand(shutdownConfig));
        manager.register(new DebugCommand());
        manager.register(new GamereportCommand(zombiesGamereportConfig));
        manager.register(new GhostCommand(playerViewProvider));
        manager.register(new FlyCommand());
        manager.register(new VelocityCommand());
        manager.register(new GamemodeCommand());
        manager.register(new AddRoleCommand(IdentitySource.MOJANG, roleStore, permissionHandler));
        manager.register(new RemoveRoleCommand(IdentitySource.MOJANG, roleStore, permissionHandler));
        manager.register(new AnnounceCommand());

        manager.getConsoleSender().addPermission(ALL_PERMISSIONS);
    }

    public static @NotNull PermissionHandler permissionHandler() {
        return FeatureUtils.check(permissionHandler);
    }
}
