package org.phantazm.server.command.server;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;

public class GamemodeCommand extends Command {
    public static final Permission PERMISSION = new Permission("admin.gamemode");

    private static final ArgumentEnum<GameMode> GAMEMODE =
            ArgumentType.Enum("gamemode", GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED);

    public GamemodeCommand() {
        super("gamemode");
        setCondition(((sender, commandString) -> sender.hasPermission(PERMISSION)));
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) {
                return;
            }

            player.setGameMode(context.get(GAMEMODE));
        }, GAMEMODE);
    }
}
