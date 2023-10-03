package org.phantazm.zombies.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.CommandUtils;
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
            Player senderPlayer = (Player) sender;
            PlayerView playerView = viewProvider.fromPlayer(senderPlayer);
            Set<ZombiesScene> scenes = SceneManager.Global.instance().typed(ZombiesScene.class);

            Optional<Scene> currentScene = SceneManager.Global.instance().currentScene(playerView);
            for (ZombiesScene zombiesScene : scenes) {
                if (!zombiesScene.managedPlayers().containsKey(playerView)) {
                    continue;
                }

                if (currentScene.isPresent() && currentScene.get() == zombiesScene) {
                    continue;
                }

                Stage stage = zombiesScene.currentStage();
                if (stage == null || !stage.canRejoin()) {
                    continue;
                }

                SuggestionEntry entry =
                    new SuggestionEntry(zombiesScene.identity().toString(), zombiesScene.mapSettingsInfo().displayName());
                suggestion.addEntry(entry);
            }
        });

        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            UUID targetGame = context.get(targetGameArgument);

            Player senderPlayer = (Player) sender;
            PlayerView playerView = viewProvider.fromPlayer(senderPlayer);

            SceneManager.Global.instance().joinScene(zombiesJoiner.rejoin(Set.of(playerView), targetGame));
        }, targetGameArgument);
    }
}
