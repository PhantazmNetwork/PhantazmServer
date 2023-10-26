package org.phantazm.zombies.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.phantazm.zombies.stage.Stage;
import org.phantazm.zombies.stage.StageKeys;
import org.phantazm.zombies.stage.StageTransition;

public class RoundCommand extends SandboxLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.round");

    private static final Argument<Integer> ROUND_NUMBER = ArgumentType.Integer("round-number").min(1)
        .setSuggestionCallback((sender, context, suggestion) -> {
            PlayerView view = PlayerViewProvider.Global.instance().fromPlayer((Player) sender);
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

    public RoundCommand() {
        super("round", PERMISSION, ROUND_NUMBER);
    }

    @Override
    protected void runCommand(@NotNull CommandContext context, @NotNull ZombiesScene scene, @NotNull Player sender) {
        boolean isRestricted = !sender.hasPermission(PERMISSION);

        RoundHandler handler = scene.map().roundHandler();
        int roundCount = handler.roundCount();
        int roundIndex = context.get(ROUND_NUMBER) - 1;

        if (!handler.isEndless() && (roundIndex < 0 || roundIndex >= roundCount)) {
            sender.sendMessage(
                Component.text("Round " + (roundIndex + 1) + " is out of bounds!", NamedTextColor.RED));
            return;
        }

        if (isRestricted && roundIndex <= handler.currentRoundIndex()) {
            sender.sendMessage(Component.text("You cannot restart the current round or go to " +
                "previous rounds!", NamedTextColor.RED));
            return;
        }

        scene.setLegit(false);

        StageTransition transition = scene.stageTransition();
        Stage current = transition.getCurrentStage();
        if (current == null || current.key().equals(StageKeys.END)) {
            sender.sendMessage(Component.text("You cannot use the round command in this stage!"));
            return;
        }

        if (!current.key().equals(StageKeys.IN_GAME)) {
            transition.setCurrentStage(StageKeys.IN_GAME);
            if (roundIndex != 0) {
                handler.setCurrentRound(roundIndex);
            }
        } else {
            handler.setCurrentRound(roundIndex);
        }
    }
}
