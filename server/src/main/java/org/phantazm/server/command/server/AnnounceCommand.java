package org.phantazm.server.command.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.phantazm.core.command.PermissionLockedCommand;

import java.util.Collection;

public class AnnounceCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.announce");

    private static final String DEFAULT_FORMAT = "<red>";

    private static final Argument<String[]> ANNOUNCEMENT = ArgumentType.StringArray("announcement");

    public AnnounceCommand() {
        super("announce", PERMISSION);

        addSyntax((sender, context) -> {
            Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
            Component message =
                    MiniMessage.miniMessage().deserialize(DEFAULT_FORMAT + String.join(" ", context.get(ANNOUNCEMENT)));
            for (Player player : players) {
                player.sendMessage(message);
            }
        }, ANNOUNCEMENT);
    }
}
