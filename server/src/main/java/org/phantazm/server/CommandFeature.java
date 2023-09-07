package org.phantazm.server;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.RouterStore;
import org.phantazm.core.game.scene.command.QuitCommand;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.player.PlayerViewProvider;

public final class CommandFeature {
    private CommandFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull RouterStore routerStore, @NotNull PlayerViewProvider viewProvider) {
        MinecraftServer.getCommandManager()
            .register(QuitCommand.quitCommand(routerStore, viewProvider));
    }

}
