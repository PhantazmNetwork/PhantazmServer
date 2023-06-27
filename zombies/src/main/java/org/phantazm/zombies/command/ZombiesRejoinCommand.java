package org.phantazm.zombies.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.zombies.scene.ZombiesJoinHelper;
import org.phantazm.zombies.scene.ZombiesScene;
import org.phantazm.zombies.scene.ZombiesSceneRouter;
import org.phantazm.zombies.stage.Stage;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ZombiesRejoinCommand extends Command {
    public ZombiesRejoinCommand(@NotNull ZombiesSceneRouter router,
            @NotNull PlayerViewProvider viewProvider, @NotNull ZombiesJoinHelper joinHelper) {
        super("rejoin");

        Objects.requireNonNull(router, "router");
        Objects.requireNonNull(viewProvider, "viewProvider");
        Objects.requireNonNull(joinHelper, "joinHelper");

        Argument<UUID> targetGameArgument = ArgumentType.UUID("target-game");
        targetGameArgument.setSuggestionCallback((sender, context, suggestion) -> {
            if (!(sender instanceof Player player)) {
                return;
            }

            Optional<ZombiesScene> currentScene = router.getCurrentScene(player.getUuid());
            for (ZombiesScene scene : router.getScenesContainingPlayer(player.getUuid())) {
                if (currentScene.isPresent() && currentScene.get() == scene) {
                    continue;
                }

                Stage stage = scene.getCurrentStage();
                if (stage != null && !stage.canRejoin()) {
                    continue;
                }

                SuggestionEntry entry =
                        new SuggestionEntry(scene.getUUID().toString(), scene.getMapSettingsInfo().displayName());
                suggestion.addEntry(entry);
            }
        });

        addConditionalSyntax((sender, commandString) -> {
            if (commandString == null) {
                return sender instanceof Player;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return false;
            }

            return true;
        }, (sender, context) -> {
            UUID targetGame = context.get(targetGameArgument);

            Optional<ZombiesScene> currentScene = router.getCurrentScene(sender.identity().uuid());

            boolean anyMatch = false;
            for (ZombiesScene scene : router.getScenesContainingPlayer(sender.identity().uuid())) {
                if (currentScene.isPresent() && currentScene.get() == scene) {
                    continue;
                }

                if (scene.getUUID().equals(targetGame)) {
                    anyMatch = true;
                    break;
                }
            }

            if (!anyMatch) {
                sender.sendMessage(Component.text("Invalid game!", NamedTextColor.RED));
                return;
            }

            joinHelper.rejoinGame(((Player)sender), targetGame);
        }, targetGameArgument);
    }
}
