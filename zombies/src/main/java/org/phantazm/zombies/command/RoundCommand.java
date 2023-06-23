package org.phantazm.zombies.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.scene.ZombiesScene;
import org.phantazm.zombies.stage.Stage;
import org.phantazm.zombies.stage.StageKeys;
import org.phantazm.zombies.stage.StageTransition;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class RoundCommand extends Command {
    public static final Permission PERMISSION = new Permission("zombies.playtest.round");

    public RoundCommand(@NotNull Function<? super UUID, Optional<ZombiesScene>> sceneMapper) {
        super("round");
        Objects.requireNonNull(sceneMapper, "sceneMapper");

        Argument<Integer> roundArgument =
                ArgumentType.Integer("round-number").min(1).setSuggestionCallback((sender, context, suggestion) -> {
                    UUID uuid = ((Player)sender).getUuid();
                    sceneMapper.apply(uuid).ifPresent(scene -> {
                        int count = scene.getMap().roundHandler().roundCount();

                        for (int i = 0; i < count; i++) {
                            suggestion.addEntry(
                                    new SuggestionEntry(Integer.toString(i + 1), Component.text("Round " + (i + 1))));
                        }
                    });
                });

        setCondition((sender, commandString) -> sender.hasPermission(PERMISSION));
        addConditionalSyntax(getCondition(), (sender, context) -> {
            UUID uuid = ((Player)sender).getUuid();
            sceneMapper.apply(uuid).ifPresent(scene -> {
                RoundHandler handler = scene.getMap().roundHandler();
                int roundCount = handler.roundCount();
                int roundIndex = context.get(roundArgument) - 1;

                if (roundIndex < 0 || roundIndex >= roundCount) {
                    sender.sendMessage(
                            Component.text("Round " + (roundIndex + 1) + " is out of bounds!", NamedTextColor.RED));
                    return;
                }

                StageTransition transition = scene.getStageTransition();
                Stage current = transition.getCurrentStage();
                if (current == null || !current.key().equals(StageKeys.IN_GAME)) {
                    transition.setCurrentStage(StageKeys.IN_GAME);
                }

                handler.currentRound().ifPresent(round -> {
                    for (PhantazmMob mob : round.getSpawnedMobs()) {
                        mob.entity().kill();
                    }
                });
                handler.setCurrentRound(roundIndex);
            });
        }, roundArgument);
    }
}
