package org.phantazm.server;

import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;

public final class ServerCommandFeature {
    static void initialize(@NotNull CommandManager commandManager) {
        Command stopCommand = new Command("stop");
        stopCommand.setDefaultExecutor((sender, context) -> PhantazmServer.shutdown("stop command"));

        commandManager.register(stopCommand);
    }
}
