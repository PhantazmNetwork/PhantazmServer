package org.phantazm.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.core.role.RoleStore;
import org.phantazm.server.command.server.*;
import org.phantazm.server.context.ConfigContext;
import org.phantazm.server.context.PlayerContext;
import org.phantazm.server.permission.PermissionHandler;
import org.phantazm.server.validator.LoginValidator;

public final class ServerCommandFeature {
    public static final Permission ALL_PERMISSIONS = new Permission("*");

    private ServerCommandFeature() {
    }

    static void initialize(@NotNull ConfigContext configContext, @NotNull PlayerContext playerContext) {
        LoginValidator validator = playerContext.loginValiator();
        RoleStore roleStore = playerContext.roles();
        boolean whitelist = configContext.serverConfig().serverInfo().whitelist();
        PermissionHandler permissionHandler = playerContext.permissionHandler();

        CommandManager manager = MinecraftServer.getCommandManager();
        manager.register(new StopCommand());
        manager.register(new BanCommand(IdentitySource.MOJANG, validator));
        manager.register(new BanHistoryCommand(IdentitySource.MOJANG, validator));
        manager.register(new BanHistoryClearCommand(IdentitySource.MOJANG, validator));
        manager.register(new PardonCommand(IdentitySource.MOJANG, validator));
        manager.register(new WhitelistCommand(IdentitySource.MOJANG, validator, whitelist));
        manager.register(new PermissionCommand(permissionHandler, IdentitySource.MOJANG));
        manager.register(new ReloadCommand());
        manager.register(new OrderlyShutdownCommand(configContext.shutdownConfig()));
        manager.register(new DebugCommand());
        manager.register(new GamereportCommand(configContext.zombiesConfig().gamereportConfig()));
        manager.register(new GhostCommand(playerContext.playerViewProvider()));
        manager.register(new FlyCommand());
        manager.register(new VelocityCommand());
        manager.register(new GamemodeCommand());
        manager.register(new AddRoleCommand(IdentitySource.MOJANG, roleStore, permissionHandler));
        manager.register(new RemoveRoleCommand(IdentitySource.MOJANG, roleStore, permissionHandler));
        manager.register(new AnnounceCommand());

        manager.getConsoleSender().addPermission(ALL_PERMISSIONS);
    }
}
