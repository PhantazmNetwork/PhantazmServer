package org.phantazm.core.scene2.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.Scene;
import org.phantazm.core.scene2.SceneManager;

import java.util.Optional;

public abstract class SceneLocalCommand<T extends Scene> extends Command {
    @SuppressWarnings("unchecked")
    public SceneLocalCommand(@NotNull String name, @NotNull Class<T> sceneType,
        @Nullable String[] aliases, @NotNull Argument<?>... arguments) {
        super(name, aliases);

        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            Player player = (Player) sender;
            PlayerView playerView = PlayerViewProvider.Global.instance().fromPlayer(player);
            Optional<T> sceneOptional = SceneManager.Global.instance().currentScene(playerView, sceneType);
            if (sceneOptional.isEmpty()) {
                sender.sendMessage(Component.text("You are not in the correct kind of scene!", NamedTextColor.RED));
                return;
            }

            T actualScene = sceneOptional.get();
            actualScene.getAcquirable().sync(self -> {
                if (!hasPermission(context, (T) self, player)) {
                    player.sendMessage(Component.text("You do not have permission to run this command here!",
                        NamedTextColor.RED));
                    return;
                }

                runCommand(context, (T) self, player);
            });
        }, arguments);
    }

    protected boolean hasPermission(@NotNull CommandContext context, @NotNull T scene, @NotNull Player sender) {
        return true;
    }

    protected abstract void runCommand(@NotNull CommandContext context, @NotNull T scene, @NotNull Player sender);
}
