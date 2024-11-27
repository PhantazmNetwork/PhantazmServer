package org.phantazm.server.command.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.phantazm.core.CommandUtils;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.core.scene2.lobby.Lobby;

/**
 * Teleports you to spawn when you are in the lobby.
 */
public class SpawnCommand extends Command {
    public static final String COMMAND_NAME = "spawn";

    public SpawnCommand() {
        super(COMMAND_NAME);

        addConditionalSyntax(CommandUtils.PLAYER_CONDITION, ((sender, context) -> {
            SceneManager sceneManager = SceneManager.Global.instance();
            Player player = (Player) sender;

            sceneManager.synchronizeWithCurrentScene(player, currentScene -> {
                if (currentScene instanceof Lobby lobby) {
                    player.teleport(lobby.getSpawnPoint());
                } else {
                    sender.sendMessage(Component.text(
                        "You can only use this command in the lobby!",
                        NamedTextColor.RED));
                }
            });
        }));
    }

}
