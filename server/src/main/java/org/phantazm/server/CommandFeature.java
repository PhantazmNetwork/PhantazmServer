package org.phantazm.server;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.command.QuitCommand;

public final class CommandFeature {
    private CommandFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull PlayerViewProvider viewProvider) {
        MinecraftServer.getCommandManager().register(QuitCommand.quitCommand(viewProvider));
    }

}
