package org.phantazm.server;

import net.minestom.server.command.CommandManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.RouterStore;
import org.phantazm.core.game.scene.command.QuitCommand;
import org.phantazm.core.player.PlayerViewProvider;

public class CommandFeature {

    static void initialize(@NotNull CommandManager commandManager, @NotNull RouterStore routerStore,
            @NotNull PlayerViewProvider viewProvider) {
        commandManager.register(QuitCommand.quitCommand(routerStore, viewProvider));
    }

}
