package org.phantazm.server.command.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.core.role.RoleStore;
import org.phantazm.server.permission.PermissionHandler;

public class AddRoleCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.add_role");

    private static final ArgumentWord PLAYER_ARGUMENT = ArgumentType.Word("player");
    private static final Argument<String> ROLE = ArgumentType.String("role");

    public AddRoleCommand(@NotNull IdentitySource identitySource, @NotNull RoleStore roleStore,
        @NotNull PermissionHandler permissionHandler) {
        super("add_role", PERMISSION);

        addSyntax((sender, context) -> {
            String name = context.get(PLAYER_ARGUMENT);
            identitySource.getUUID(name).thenAccept((uuidOptional) -> {
                uuidOptional.ifPresent(uuid -> {
                    String role = context.get(ROLE);

                    roleStore.giveRole(uuid, role).thenAccept((result) -> {
                        if (result) {
                            sender.sendMessage("Gave " + uuid + " (" + name + ") role " + role);
                            permissionHandler.applyPermissions(uuid);
                        } else {
                            sender.sendMessage(Component.text("Failed to add role. The player may already " +
                                "have it, or it may not be a known role.", NamedTextColor.RED));
                        }
                    });
                });
            });
        }, PLAYER_ARGUMENT, ROLE);
    }
}
