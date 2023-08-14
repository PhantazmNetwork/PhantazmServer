package org.phantazm.server.command.server;

import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.command.PermissionLockedCommand;

public class FlyCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.fly");

    public FlyCommand() {
        super("fly", PERMISSION);

        addConditionalSyntax(CommandUtils.playerSenderCondition(), ((sender, context) -> {
            Player player = (Player)sender;
            boolean isAllowFlying = player.isAllowFlying();
            player.setAllowFlying(!isAllowFlying);
            if (isAllowFlying) {
                player.setFlying(false);
            }
        }));
    }
}
