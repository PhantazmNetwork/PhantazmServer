package org.phantazm.server;

import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;

public final class ServerCommandFeature {
    public static final Permission STOP_PERMISSION = new Permission("stop");

    static void initialize(@NotNull CommandManager commandManager) {
        commandManager.getConsoleSender().getAllPermissions().add(STOP_PERMISSION);

        Command stopCommand = new Command("stop");
        stopCommand.setDefaultExecutor((sender, context) -> PhantazmServer.shutdown("stop command"));
        stopCommand.setCondition((sender, commandString) -> sender.hasPermission(STOP_PERMISSION));

        commandManager.register(stopCommand);
    }
}
