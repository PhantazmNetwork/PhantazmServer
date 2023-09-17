package org.phantazm.server;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.scene2.command.QuitCommand;
import org.phantazm.core.player.PlayerViewProvider;

public final class CommandFeature {
    private CommandFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull PlayerViewProvider viewProvider) {
        MinecraftServer.getCommandManager().register(QuitCommand.quitCommand(viewProvider));
    }

}
