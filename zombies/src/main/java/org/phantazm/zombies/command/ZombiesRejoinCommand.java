package org.phantazm.zombies.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.scene.ZombiesJoinHelper;
import org.phantazm.zombies.scene.ZombiesScene;
import org.phantazm.zombies.scene.ZombiesSceneRouter;

import java.util.Objects;
import java.util.UUID;

public class ZombiesRejoinCommand extends Command {
    public ZombiesRejoinCommand(@NotNull ZombiesSceneRouter router, @NotNull ZombiesJoinHelper joinHelper) {
        super("rejoin");

        Objects.requireNonNull(router, "router");

        Argument<UUID> targetGameArgument = ArgumentType.UUID("target-game");
        targetGameArgument.setSuggestionCallback(((sender, context, suggestion) -> {
            if (!(sender instanceof Player player)) {
                return;
            }

            UUID uuid = player.getUuid();
            for (ZombiesScene scene : router.getScenes()) {
                if (!scene.getZombiesPlayers().containsKey(uuid)) {
                    continue;
                }

                SuggestionEntry entry =
                        new SuggestionEntry(scene.getUUID().toString(), scene.getMapSettingsInfo().displayName());
                suggestion.addEntry(entry);
            }
        }));

        addConditionalSyntax((sender, commandString) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return false;
            }

            return true;
        }, (sender, context) -> {
            UUID targetGame = context.get(targetGameArgument);

            boolean anyMatch = false;
            for (ZombiesScene scene : router.getScenes()) {
                if (scene.getUUID().equals(targetGame)) {
                    anyMatch = true;
                    break;
                }
            }

            if (!anyMatch) {
                sender.sendMessage(Component.text("Invalid game!", NamedTextColor.RED));
                return;
            }

            joinHelper.rejoinGame(((Player) sender), targetGame);
        }, targetGameArgument);
    }
}
