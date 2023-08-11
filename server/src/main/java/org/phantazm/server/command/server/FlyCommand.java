package org.phantazm.server.command.server;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;

public class FlyCommand extends Command {
    public static final Permission PERMISSION = new Permission("admin.fly");
    public FlyCommand() {
        super("fly");
        setCondition(((sender, commandString) -> sender.hasPermission(PERMISSION)));
        addConditionalSyntax(getCondition(), ((sender, context) -> {
            if (!(sender instanceof Player player)) {
                return;
            }
            boolean isAllowFlying = player.isAllowFlying();
            player.setAllowFlying(!isAllowFlying);
            if (isAllowFlying) {
                player.setFlying(false);
            }
        }));
    }
}
