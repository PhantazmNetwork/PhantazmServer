package org.phantazm.core.game.scene.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.Scene;
import org.phantazm.core.game.scene.TransferResult;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public final class QuitCommand {

    private QuitCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command quitCommand(
            @NotNull Function<? super UUID, Optional<? extends Scene<?>>> sceneMapper,
            @NotNull PlayerViewProvider viewProvider) {
        Objects.requireNonNull(sceneMapper, "sceneMapper");
        Objects.requireNonNull(viewProvider, "viewProvider");

        Command command = new Command("quit");
        command.addConditionalSyntax((sender, commandString) -> {
            if (commandString == null) {
                return sender instanceof Player;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return false;
            }

            return true;
        }, (sender, context) -> {
            Player player = (Player)sender;
            sceneMapper.apply(player.getUuid()).ifPresent(scene -> {
                if (!scene.isQuittable()) {
                    sender.sendMessage(Component.text("You can't quit this scene.", NamedTextColor.RED));
                    return;
                }

                TransferResult result = scene.leave(Collections.singleton(player.getUuid()));
                if (result.success()) {
                    scene.getFallback().fallback(viewProvider.fromPlayer(player));
                }
                else {
                    result.message().ifPresent(sender::sendMessage);
                }
            });
        });

        return command;
    }
}
