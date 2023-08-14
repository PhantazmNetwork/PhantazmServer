package org.phantazm.server.command.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.permission.Permission;

public class DebugCommand extends Command {
    public static final Permission PERMISSION = new Permission("admin.debug");
    public static final Permission EVENT_PERMISSION = new Permission("admin.debug.event");
    public static final Permission INSTANCE_PERMISSION = new Permission("admin.debug.instance");

    public DebugCommand() {
        super("debug");
        setCondition((sender, commandString) -> sender.hasPermission(PERMISSION));

        addSubcommand(new DebugEvent());
        addSubcommand(new DebugInstance());
    }

    private static class DebugEvent extends Command {
        private DebugEvent() {
            super("event");

            setCondition((sender, commandString) -> sender.hasPermission(EVENT_PERMISSION));
            addSyntax((sender, context) -> sender.sendMessage(MinecraftServer.getGlobalEventHandler().toString()));
        }
    }

    private static class DebugInstance extends Command {
        private DebugInstance() {
            super("instance");

            setCondition((sender, commandString) -> sender.hasPermission(INSTANCE_PERMISSION));
            addSyntax((sender, context) -> {
                for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
                    sender.sendMessage(instance.getUniqueId() + ": " +
                            instance.getEntityTracker().entities(EntityTracker.Target.PLAYERS).size() + " player");
                }
            });
        }
    }
}
