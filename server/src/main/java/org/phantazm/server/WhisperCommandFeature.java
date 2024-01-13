package org.phantazm.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.server.command.whisper.ReplyCommand;
import org.phantazm.server.command.whisper.WhisperCommand;
import org.phantazm.server.command.whisper.WhisperManager;
import org.phantazm.server.context.ConfigContext;

public class WhisperCommandFeature {
    static void initialize(@NotNull ConfigContext configContext) {
        ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
        CommandManager commandManager = MinecraftServer.getCommandManager();
        WhisperManager whisperManager =
            new WhisperManager(connectionManager, commandManager.getConsoleSender(), configContext.whisperConfig());

        commandManager.register(WhisperCommand.whisperCommand(connectionManager, whisperManager));
        commandManager.register(ReplyCommand.replyCommand(whisperManager));
    }

}
