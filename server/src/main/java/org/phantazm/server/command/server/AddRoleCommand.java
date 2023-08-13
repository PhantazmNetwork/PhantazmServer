package org.phantazm.server.command.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.server.permission.PermissionHandler;
import org.phantazm.server.role.RoleStore;

public class AddRoleCommand extends Command {
    public static final Permission PERMISSION = new Permission("admin.add_role");

    private static final ArgumentWord PLAYER_ARGUMENT = ArgumentType.Word("player");
    private static final Argument<String> ROLE = ArgumentType.String("role");

    public AddRoleCommand(@NotNull IdentitySource identitySource, @NotNull RoleStore roleStore,
            @NotNull PermissionHandler permissionHandler) {
        super("add_role");

        setCondition((sender, commandString) -> sender.hasPermission(PERMISSION));
        addConditionalSyntax(getCondition(), (sender, context) -> {
            String name = context.get(PLAYER_ARGUMENT);
            identitySource.getUUID(name).whenComplete((uuidOptional, throwable) -> {
                uuidOptional.ifPresent(uuid -> {
                    String role = context.get(ROLE);

                    roleStore.giveRole(uuid, role).whenComplete((result, error) -> {
                        if (error != null) {
                            sender.sendMessage(
                                    Component.text("An internal error occured while executing " + "this command.")
                                            .color(NamedTextColor.RED));
                            LOGGER.warn("An exception occurred while adding a role", error);
                            return;
                        }

                        if (result) {
                            sender.sendMessage("Gave " + uuid + " (" + name + ") role " + role);
                            permissionHandler.applyPermissions(uuid, sender);
                        }
                        else {
                            sender.sendMessage(Component.text("Failed to add role. The player may already " +
                                    "have it, or it may not be a known role.", NamedTextColor.RED));
                        }
                    });
                });
            });
        }, PLAYER_ARGUMENT, ROLE);
    }
}
