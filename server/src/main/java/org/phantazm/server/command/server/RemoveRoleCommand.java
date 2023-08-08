package org.phantazm.server.command.server;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.server.role.RoleStore;

public class RemoveRoleCommand extends Command {
    public static final Permission PERMISSION = new Permission("admin.remove_role");

    private static final ArgumentWord PLAYER_ARGUMENT = ArgumentType.Word("player");
    private static final Argument<String> ROLE = ArgumentType.String("role");

    public RemoveRoleCommand(@NotNull IdentitySource identitySource, @NotNull RoleStore roleStore) {
        super("remove_role");

        setCondition((sender, commandString) -> sender.hasPermission(PERMISSION));
        addConditionalSyntax(getCondition(), (sender, context) -> {
            String name = context.get(PLAYER_ARGUMENT);
            identitySource.getUUID(name).whenComplete((uuidOptional, throwable) -> {
                uuidOptional.ifPresent(uuid -> {
                    roleStore.removeRole(uuid, context.get(ROLE));
                });
            });
        }, PLAYER_ARGUMENT, ROLE);
    }
}
