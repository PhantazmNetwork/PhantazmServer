package org.phantazm.zombies.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import net.minestom.server.timer.SchedulerManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.scene.ZombiesScene;
import org.phantazm.zombies.stage.Stage;
import org.phantazm.zombies.stage.StageKeys;
import org.phantazm.zombies.stage.StageTransition;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class RoundCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.round");

    public RoundCommand(@NotNull Function<? super UUID, Optional<ZombiesScene>> sceneMapper,
        @NotNull SchedulerManager schedulerManager) {
        super("round", PERMISSION);
        Objects.requireNonNull(sceneMapper);

        Argument<Integer> roundArgument =
            ArgumentType.Integer("round-number").min(1).setSuggestionCallback((sender, context, suggestion) -> {
                UUID uuid = ((Player) sender).getUuid();
                sceneMapper.apply(uuid).ifPresent(scene -> {
                    int count = scene.getMap().roundHandler().roundCount();

                    for (int i = 0; i < count; i++) {
                        suggestion.addEntry(
                            new SuggestionEntry(Integer.toString(i + 1), Component.text("Round " + (i + 1))));
                    }
                });
            });

        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            UUID uuid = ((Player) sender).getUuid();
            sceneMapper.apply(uuid).ifPresent(scene -> {
                RoundHandler handler = scene.getMap().roundHandler();
                int roundCount = handler.roundCount();
                int roundIndex = context.get(roundArgument) - 1;

                if (roundIndex < 0 || roundIndex >= roundCount) {
                    sender.sendMessage(
                        Component.text("Round " + (roundIndex + 1) + " is out of bounds!", NamedTextColor.RED));
                    return;
                }

                scene.setLegit(false);

                StageTransition transition = scene.getStageTransition();
                Stage current = transition.getCurrentStage();
                schedulerManager.scheduleNextProcess(() -> {
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
        }, roundArgument);
    }
}
