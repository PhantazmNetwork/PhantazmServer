package org.phantazm.core.game.scene.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.TransferResult;
import org.phantazm.core.game.scene.Scene;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class QuitCommand extends Command {
    public QuitCommand(@NotNull Function<? super UUID, Optional<? extends Scene<?>>> sceneMapper,
            @NotNull SceneFallback fallback, @NotNull PlayerViewProvider viewProvider) {
        super("quit");

        Objects.requireNonNull(sceneMapper, "sceneMapper");

        addSyntax((sender, context) -> {
            Player player = (Player)sender;
            sceneMapper.apply(player.getUuid()).ifPresent(scene -> {
                TransferResult result = scene.leave(Collections.singleton(player.getUuid()));
                if (result.success()) {
                    fallback.fallback(viewProvider.fromPlayer(player));
                }
                else {
                    result.message().ifPresent(sender::sendMessage);
                }
            });
        });
    }
}
