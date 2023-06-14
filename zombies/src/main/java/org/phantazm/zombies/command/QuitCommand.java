package org.phantazm.zombies.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.RouteResult;
import org.phantazm.core.game.scene.Scene;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.zombies.scene.ZombiesRouteRequest;

import java.util.Collections;

public class QuitCommand extends Command {
    public QuitCommand(@NotNull Scene<ZombiesRouteRequest> router, @NotNull SceneFallback fallback,
            @NotNull PlayerViewProvider viewProvider) {
        super("quit");

        addConditionalSyntax((sender, commandString) -> {
            if (sender instanceof Player) {
                return true;
            }

            sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
            return false;
        }, (sender, context) -> {
            Player player = (Player)sender;
            RouteResult result = router.leave(Collections.singleton(player.getUuid()));
            if (result.success()) {
                fallback.fallback(viewProvider.fromPlayer(player));
            } else {
                result.message().ifPresent(sender::sendMessage);
            }
        });
    }
}
