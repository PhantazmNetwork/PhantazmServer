package org.phantazm.server.command.server;

import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.command.PermissionLockedCommand;

public class GamemodeCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.gamemode");

    private static final ArgumentEnum<GameMode> GAMEMODE =
            ArgumentType.Enum("gamemode", GameMode.class).setFormat(ArgumentEnum.Format.LOWER_CASED);

    public GamemodeCommand() {
        super("gamemode", PERMISSION);

        addConditionalSyntax(CommandUtils.playerSenderCondition(), (sender, context) -> {
            Player player = (Player)sender;
            player.setGameMode(context.get(GAMEMODE));
        }, GAMEMODE);
    }
}
