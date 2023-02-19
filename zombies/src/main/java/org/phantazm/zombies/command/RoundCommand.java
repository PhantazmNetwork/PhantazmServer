package org.phantazm.zombies.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class RoundCommand extends Command {
    public RoundCommand(@NotNull Function<? super UUID, ? extends Optional<ZombiesScene>> sceneMapper) {
        super("round");
        Objects.requireNonNull(sceneMapper, "sceneMapper");

        Argument<Integer> roundArgument = ArgumentType.Integer("round").min(1);
        roundArgument.setSuggestionCallback((sender, context, suggestion) -> {
            UUID uuid = ((Player)sender).getUuid();
            sceneMapper.apply(uuid).ifPresent(scene -> {
                int count = scene.getMap().roundHandler().roundCount();

                for (int i = 0; i < count; i++) {
                    suggestion.addEntry(
                            new SuggestionEntry(Integer.toString(i + 1), Component.text("Round " + (i + 1))));
                }
            });
        });

        addConditionalSyntax((sender, commandString) -> sender instanceof Player, (sender, context) -> {
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

                handler.setCurrentRound(roundIndex);
            });
        }, roundArgument);
    }
}
