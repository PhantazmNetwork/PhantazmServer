package org.phantazm.server.command.server;

import net.minestom.server.permission.Permission;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.server.PhantazmServer;

public class StopCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.stop");

    public StopCommand() {
        super("stop", PERMISSION);

        addSyntax((sender, context) -> PhantazmServer.shutdown("stop command"));
    }
}
