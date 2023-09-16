package org.phantazm.core.scene2;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.CoreJoinKeys;
import org.phantazm.core.scene2.SceneManager;

import java.util.Objects;
import java.util.Set;

public final class QuitCommand {
    private QuitCommand() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull Command quitCommand(@NotNull PlayerViewProvider viewProvider) {
        Objects.requireNonNull(viewProvider);

        Command command = new Command("quit", "leave", "l");
        command.addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            Player player = (Player) sender;
            PlayerView playerView = viewProvider.fromPlayer(player);
            SceneManager.Global.instance().currentScene(playerView).ifPresent(scene -> {
                if (!scene.isGame()) {
                    sender.sendMessage(Component.text("You can't quit this scene.", NamedTextColor.RED));
                    return;
                }

                SceneManager.Global.instance().joinScene(CoreJoinKeys.MAIN_LOBBY, Set.of(playerView));
            });
        });

        return command;
    }
}
