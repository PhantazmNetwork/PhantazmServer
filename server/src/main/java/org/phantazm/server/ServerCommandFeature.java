package org.phantazm.server;

import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.server.command.BanCommand;
import org.phantazm.server.command.PardonCommand;
import org.phantazm.server.command.WhitelistCommand;
import org.phantazm.server.player.LoginValidator;

public final class ServerCommandFeature {
    static void initialize(@NotNull CommandManager commandManager, @NotNull LoginValidator loginValidator,
            boolean whitelist) {
        Command stopCommand = new Command("stop");
        stopCommand.setDefaultExecutor((sender, context) -> PhantazmServer.shutdown("stop command"));

        commandManager.register(stopCommand);

        commandManager.register(new BanCommand(IdentitySource.MOJANG, loginValidator));
        commandManager.register(new PardonCommand(IdentitySource.MOJANG, loginValidator));
        commandManager.register(new WhitelistCommand(IdentitySource.MOJANG, loginValidator, whitelist));
    }
}
