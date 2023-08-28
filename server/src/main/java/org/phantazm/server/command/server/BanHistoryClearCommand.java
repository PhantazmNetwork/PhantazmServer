package org.phantazm.server.command.server;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.server.validator.LoginValidator;

public class BanHistoryClearCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.ban_history_clear");

    private static final ArgumentWord PLAYER_ARGUMENT = ArgumentType.Word("player");

    public BanHistoryClearCommand(@NotNull IdentitySource identitySource, @NotNull LoginValidator loginValidator) {
        super("ban_history_clear", PERMISSION);

        addSyntax((sender, context) -> {
            String name = context.get(PLAYER_ARGUMENT);
            identitySource.getUUID(name).whenComplete((uuidOptional, throwable) -> {
                uuidOptional.ifPresent(loginValidator::clearHistory);
            });
        }, PLAYER_ARGUMENT);
    }
}
