package org.phantazm.server.command.server;

import net.minestom.server.command.builder.Command;
import net.minestom.server.permission.Permission;
import org.phantazm.server.PhantazmServer;

public class StopCommand extends Command {
    public static final Permission PERMISSION = new Permission("admin.stop");

    public StopCommand() {
        super("stop");

        setCondition((sender, commandString) -> sender.hasPermission(PERMISSION));
        addConditionalSyntax(getCondition(), (sender, context) -> PhantazmServer.shutdown("stop command"));
    }
}
