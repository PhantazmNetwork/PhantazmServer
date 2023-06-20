package org.phantazm.zombies.command;

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

        setCondition((sender, commandString) -> sender.hasPermission(Permissions.PLAYTEST));
        addConditionalSyntax(getCondition(), (sender, context) -> {
            Player player = (Player)sender;
            RouteResult result = router.leave(Collections.singleton(player.getUuid()));
            if (result.success()) {
                fallback.fallback(viewProvider.fromPlayer(player));
            }
            else {
                result.message().ifPresent(sender::sendMessage);
            }
        });
    }
}
