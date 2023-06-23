package org.phantazm.server;

import net.minestom.server.command.CommandManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.Scene;
import org.phantazm.core.game.scene.command.QuitCommand;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class CommandFeature {

    static void initialize(@NotNull CommandManager commandManager, @NotNull Function<? super UUID, Optional<?
            extends Scene<?>>> sceneMapper, @NotNull PlayerViewProvider viewProvider) {
        commandManager.register(QuitCommand.quitCommand(sceneMapper, viewProvider));
    }

}
