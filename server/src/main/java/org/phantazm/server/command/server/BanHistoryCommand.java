package org.phantazm.server.command.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.server.validator.LoginValidator;

import java.time.Instant;

public class BanHistoryCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.ban_history");

    private static final ArgumentWord PLAYER_ARGUMENT = ArgumentType.Word("player");

    public BanHistoryCommand(@NotNull IdentitySource identitySource, @NotNull LoginValidator loginValidator) {
        super("ban_history", PERMISSION);

        addSyntax((sender, context) -> {
            String name = context.get(PLAYER_ARGUMENT);
            identitySource.getUUID(name).whenComplete((uuidOptional, throwable) -> {
                uuidOptional.ifPresent(uuid -> {
                    loginValidator.history(uuid).thenAccept(banHistory -> {
                        if (banHistory.lastBanDate() < 0) {
                            sender.sendMessage(Component.text(name + " has never been banned!", NamedTextColor.RED));
                            return;
                        }

                        Instant instant = Instant.ofEpochSecond(banHistory.lastBanDate());
                        int banCount = banHistory.banCount();

                        sender.sendMessage("Ban record for " + name);
                        sender.sendMessage("Last banned: " + instant);
                        sender.sendMessage("Times banned: " + banCount);
                    });
                });
            });
        }, PLAYER_ARGUMENT);
    }
}