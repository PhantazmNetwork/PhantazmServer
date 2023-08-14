package org.phantazm.server.command.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.permission.Permission;
import org.phantazm.core.command.PermissionLockedCommand;

public class DebugCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.debug");
    public static final Permission EVENT_PERMISSION = new Permission("admin.debug.event");
    public static final Permission INSTANCE_PERMISSION = new Permission("admin.debug.instance");

    public DebugCommand() {
        super("debug", PERMISSION);
        addSubcommand(new DebugEvent());
        addSubcommand(new DebugInstance());
    }

    private static class DebugEvent extends PermissionLockedCommand {
        private DebugEvent() {
            super("event", EVENT_PERMISSION);
            addSyntax((sender, context) -> sender.sendMessage(MinecraftServer.getGlobalEventHandler().toString()));
        }
    }

    private static class DebugInstance extends PermissionLockedCommand {
        private DebugInstance() {
            super("instance", INSTANCE_PERMISSION);
            addSyntax((sender, context) -> {
                for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
                    sender.sendMessage(instance.getUniqueId() + ": " +
                            instance.getEntityTracker().entities(EntityTracker.Target.PLAYERS).size() + " player");
                }
            });
        }
    }
}
