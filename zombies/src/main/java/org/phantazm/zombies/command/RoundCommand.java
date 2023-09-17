package org.phantazm.zombies.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.phantazm.zombies.stage.Stage;
import org.phantazm.zombies.stage.StageKeys;
import org.phantazm.zombies.stage.StageTransition;

public class RoundCommand extends SandboxCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.round");

    public RoundCommand(@NotNull PlayerViewProvider viewProvider) {
        super("round", PERMISSION);

        Argument<Integer> roundArgument =
            ArgumentType.Integer("round-number").min(1).setSuggestionCallback((sender, context, suggestion) -> {
                PlayerView view = viewProvider.fromPlayer((Player) sender);
                SceneManager.Global.instance().currentScene(view).ifPresent(scene -> {
                    if (!(scene instanceof ZombiesScene zombiesScene)) {
                        return;
                    }

                    int count = zombiesScene.map().roundHandler().roundCount();
                    for (int i = 0; i < count; i++) {
                        suggestion.addEntry(
                            new SuggestionEntry(Integer.toString(i + 1), Component.text("Round " + (i + 1))));
                    }
                });
            });

        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            Player playerSender = (Player) sender;
            PlayerView playerView = viewProvider.fromPlayer(playerSender);
            SceneManager.Global.instance().currentScene(playerView, ZombiesScene.class).ifPresent(scene -> {
                if (super.cannotExecute(sender, scene)) {
                    return;
                }

                scene.getAcquirable().sync(self -> {
                    RoundHandler handler = self.map().roundHandler();
                    int roundCount = handler.roundCount();
                    int roundIndex = context.get(roundArgument) - 1;

                    if (roundIndex < 0 || roundIndex >= roundCount) {
                        sender.sendMessage(
                            Component.text("Round " + (roundIndex + 1) + " is out of bounds!", NamedTextColor.RED));
                        return;
                    }

                    self.setLegit(false);

                    StageTransition transition = self.stageTransition();
                    Stage current = transition.getCurrentStage();
                    MinecraftServer.getSchedulerManager().scheduleNextProcess(() -> {
                        if (current == null || !current.key().equals(StageKeys.IN_GAME)) {
                            transition.setCurrentStage(StageKeys.IN_GAME);
                            if (roundIndex != 0) {
                                handler.setCurrentRound(roundIndex);
                            }
                        } else {
                            handler.setCurrentRound(roundIndex);
                        }
                    });
                });
            });
        }, roundArgument);
    }
}
