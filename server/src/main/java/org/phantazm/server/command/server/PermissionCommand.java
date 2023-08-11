package org.phantazm.server.command.server;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.server.permission.PermissionHandler;

import java.util.UUID;

public class PermissionCommand extends Command {
    public static final Permission PERMISSION = new Permission("admin.permission");

    public static final Permission PERMISSION_RELOAD = new Permission("admin.permission.reload");

    public static final Permission PERMISSION_ADD_GROUP = new Permission("admin.permission.group.add");
    public static final Permission PERMISSION_REMOVE_GROUP = new Permission("admin.permission.group.remove");

    public static final Permission PERMISSION_GROUP_SET = new Permission("admin.permission.group.set");
    public static final Permission PERMISSION_GROUP_CLEAR = new Permission("admin.permission.group.clear");

    private static final ArgumentWord PLAYER_ARGUMENT = ArgumentType.Word("player");
    private static final ArgumentWord GROUP_ARGUMENT = ArgumentType.Word("group");
    private static final ArgumentWord PERMISSION_ARGUMENT = ArgumentType.Word("permission");

    public PermissionCommand(@NotNull PermissionHandler permissionHandler, @NotNull IdentitySource identitySource) {
        super("permission");

        setCondition((sender, commandString) -> sender.hasPermission(PERMISSION));
        addSubcommand(new Add(permissionHandler));
        addSubcommand(new Remove(permissionHandler));
        addSubcommand(new GroupSet(permissionHandler, identitySource));
        addSubcommand(new GroupClear(permissionHandler, identitySource));
        addSubcommand(new Reload(permissionHandler));
    }

    private static class Reload extends Command {
        private Reload(PermissionHandler permissionHandler) {
            super("reload");

            setCondition((sender, commandString) -> sender.hasPermission(PERMISSION_RELOAD));
            addConditionalSyntax(getCondition(), (sender, context) -> {
                permissionHandler.reload();
            });
        }
    }

    private static class GroupSet extends Command {
        private GroupSet(PermissionHandler permissionHandler, IdentitySource identitySource) {
            super("group_set");

            setCondition((sender, commandString) -> sender.hasPermission(PERMISSION_GROUP_SET));
            addConditionalSyntax(getCondition(), (sender, context) -> {
                String group = context.get(GROUP_ARGUMENT);
                String player = context.get(PLAYER_ARGUMENT);
                identitySource.getUUID(player).whenComplete((uuidOptional, throwable) -> {
                    if (uuidOptional.isPresent()) {
                        UUID uuid = uuidOptional.get();
                        permissionHandler.addToGroup(uuid, group);
                        sender.sendMessage("Added " + uuid + " (" + player + ") to group " + group);
                    }
                    else {
                        sender.sendMessage("Error when resolving player UUID");
                    }
                });
            }, GROUP_ARGUMENT, PLAYER_ARGUMENT);
        }
    }

    private static class GroupClear extends Command {
        private GroupClear(PermissionHandler permissionHandler, IdentitySource identitySource) {
            super("group_clear");

            setCondition((sender, commandString) -> sender.hasPermission(PERMISSION_GROUP_CLEAR));
            addConditionalSyntax(getCondition(), (sender, context) -> {
                String group = context.get(GROUP_ARGUMENT);
                String player = context.get(PLAYER_ARGUMENT);
                identitySource.getUUID(player).whenComplete((uuidOptional, throwable) -> {
                    if (uuidOptional.isPresent()) {
                        UUID uuid = uuidOptional.get();
                        permissionHandler.removeFromGroup(uuid, group);
                        sender.sendMessage("Removed " + uuid + " (" + player + ") from group " + group);
                    }
                    else {
                        sender.sendMessage("Error when resolving player UUID");
                    }
                });
            }, GROUP_ARGUMENT, PLAYER_ARGUMENT);
        }
    }

    private static class Add extends Command {
        private Add(PermissionHandler permissionHandler) {
            super("add");

            setCondition((sender, commandString) -> sender.hasPermission(PERMISSION_ADD_GROUP));
            addConditionalSyntax(getCondition(), (sender, context) -> {
                String group = context.get(GROUP_ARGUMENT);
                String permission = context.get(PERMISSION_ARGUMENT);

                permissionHandler.addGroupPermission(group, new Permission(permission));
                sender.sendMessage("Added permission " + permission + " to group " + group);
            }, GROUP_ARGUMENT, PERMISSION_ARGUMENT);
        }
    }

    private static class Remove extends Command {
        private Remove(PermissionHandler permissionHandler) {
            super("remove");

            setCondition((sender, commandString) -> sender.hasPermission(PERMISSION_REMOVE_GROUP));
            addConditionalSyntax(getCondition(), (sender, context) -> {
                String group = context.get(GROUP_ARGUMENT);
                String permission = context.get(PERMISSION_ARGUMENT);

                permissionHandler.removeGroupPermission(group, new Permission(permission));
                sender.sendMessage("Removed permission " + permission + " from group " + group);
            }, GROUP_ARGUMENT, PERMISSION_ARGUMENT);
        }
    }
}
