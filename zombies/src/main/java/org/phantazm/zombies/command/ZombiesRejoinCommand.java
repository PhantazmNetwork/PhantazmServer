package org.phantazm.zombies.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.CommandUtils;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.Scene;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.zombies.scene2.ZombiesJoiner;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.phantazm.zombies.stage.Stage;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ZombiesRejoinCommand extends Command {
    public ZombiesRejoinCommand(@NotNull ZombiesJoiner zombiesJoiner) {
        super("rejoin");

        PlayerViewProvider viewProvider = PlayerViewProvider.Global.instance();
        Argument<UUID> targetGameArgument = ArgumentType.UUID("target-game").setDefaultValue(() -> null);

        targetGameArgument.setSuggestionCallback((sender, context, suggestion) -> {
            if (!(sender instanceof Player player)) return;

            PlayerView playerView = viewProvider.fromPlayer(player);
            Set<ZombiesScene> scenes = SceneManager.Global.instance().typed(ZombiesScene.class);

            Optional<Scene> currentScene = SceneManager.Global.instance().currentScene(playerView);

            CommandUtils.tabComplete(suggestion, scenes, sceneEntry -> {
                if (sceneEntry.managedPlayers().containsKey(playerView)) return null;
                if (currentScene.isPresent() && currentScene.get() == sceneEntry) return null;

                Stage stage = sceneEntry.currentStage();
                if (stage == null || !stage.canRejoin()) return null;

                return sceneEntry.identity().toString();
            }, null, sceneEntry -> {
                return sceneEntry.mapSettingsInfo().displayName();
            });
        });

        addConditionalSyntax(CommandUtils.PLAYER_CONDITION, (sender, context) -> {
            UUID targetGame = context.get(targetGameArgument);

            Player senderPlayer = (Player) sender;
            PlayerView playerView = viewProvider.fromPlayer(senderPlayer);

            SceneManager.Global.instance().joinScene(zombiesJoiner.rejoin(Set.of(playerView), targetGame));
        }, targetGameArgument);
    }
}
