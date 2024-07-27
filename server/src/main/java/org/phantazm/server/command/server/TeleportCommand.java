package org.phantazm.server.command.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.core.scene2.SceneManager;

import java.util.Collection;

public class TeleportCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.teleport");

    private static final Argument<String> TELEPORTING_PLAYER = ArgumentType.String("teleportingPlayer");
    private static final Argument<String> DESTINATION_PLAYER = ArgumentType.String("destinationPlayer");

    public TeleportCommand() {
        super("tp", PERMISSION);

        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            Player teleportingPlayer = MinecraftServer.getConnectionManager().getPlayer(context.get(TELEPORTING_PLAYER));
            if(teleportingPlayer == null) {
                sender.sendMessage(Component.text(context.get(TELEPORTING_PLAYER) + " is not online!",
                    NamedTextColor.RED));
                return;
            }

            Player destinationPlayer = MinecraftServer.getConnectionManager().getPlayer(context.get(DESTINATION_PLAYER));
            if(destinationPlayer == null) {
                sender.sendMessage(Component.text(context.get(DESTINATION_PLAYER) + " is not online!",
                    NamedTextColor.RED));

                return;
            }

            SceneManager.Global.instance().synchronizeWithCurrentScene(teleportingPlayer, currentScene -> {
                Collection<Player> players = currentScene.getPlayers();
                for(Player player : players) {
                    if(player.equals(destinationPlayer)) {
                        Pos pos = player.getPosition();
                        teleportingPlayer.teleport(pos);
                        return;
                    }
                }
                sender.sendMessage(Component.text("The players are not in the same scene!", NamedTextColor.RED));
            });
        }, TELEPORTING_PLAYER, DESTINATION_PLAYER);
    }
}
