package org.phantazm.server.command.server;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.server.validator.LoginValidator;

public class PardonCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.pardon");

    private static final ArgumentWord PLAYER_ARGUMENT = ArgumentType.Word("player");
    private static final Argument<String> REASON = ArgumentType.String("reason").setDefaultValue("");

    public PardonCommand(@NotNull IdentitySource identitySource, @NotNull LoginValidator loginValidator) {
        super("pardon", PERMISSION);

        addSyntax((sender, context) -> {
            String name = context.get(PLAYER_ARGUMENT);
            identitySource.getUUID(name).thenAccept((uuidOptional) -> {
                uuidOptional.ifPresent(uuid -> {
                    if (loginValidator.isBanned(uuid)) {
                        loginValidator.pardon(uuid);
                        sender.sendMessage("Pardoned " + uuid + " (" + name + ")");
                    } else {
                        sender.sendMessage(uuid + " (" + name + ") is not banned");
                    }
                });
            });
        }, PLAYER_ARGUMENT, REASON);
    }
}
