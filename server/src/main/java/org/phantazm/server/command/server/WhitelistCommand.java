package org.phantazm.server.command.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.server.validator.LoginValidator;

public class WhitelistCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.whitelist");
    public static final Permission PERMISSION_ADD = new Permission("admin.whitelist.add");
    public static final Permission PERMISSION_REMOVE = new Permission("admin.whitelist.remove");
    private static final ArgumentWord PLAYER_ARGUMENT = ArgumentType.Word("player");

    public WhitelistCommand(@NotNull IdentitySource identitySource, @NotNull LoginValidator loginValidator,
        boolean whitelist) {
        super("whitelist", PERMISSION);

        addSubcommand(new Add(identitySource, loginValidator));
        addSubcommand(new Remove(identitySource, loginValidator, whitelist));
    }

    private static class Add extends PermissionLockedCommand {
        private Add(@NotNull IdentitySource identitySource, @NotNull LoginValidator loginValidator) {
            super("add", PERMISSION_ADD);

            addSyntax((sender, context) -> {
                String name = context.get(PLAYER_ARGUMENT);
                identitySource.getUUID(name).whenComplete((uuidOptional, throwable) -> {
                    uuidOptional.ifPresent(uuid -> {
                        if (loginValidator.isWhitelisted(uuid)) {
                            sender.sendMessage(uuid + " (" + name + ") is already whitelisted");
                        } else {
                            loginValidator.addWhitelist(uuid);
                            sender.sendMessage("Whitelisted " + uuid + " (" + name + ")");
                        }
                    });
                });
            }, PLAYER_ARGUMENT);
        }
    }

    private static class Remove extends PermissionLockedCommand {
        private Remove(@NotNull IdentitySource identitySource, @NotNull LoginValidator loginValidator,
            boolean whitelist) {
            super("remove", PERMISSION_REMOVE);

            addSyntax((sender, context) -> {
                String name = context.get(PLAYER_ARGUMENT);
                identitySource.getUUID(name).whenComplete((uuidOptional, throwable) -> {
                    uuidOptional.ifPresent(uuid -> {
                        if (loginValidator.isWhitelisted(uuid)) {
                            loginValidator.removeWhitelist(uuid);
                            sender.sendMessage("Removed " + uuid + " (" + name + ") from the whitelist");

                            if (whitelist) {
                                Player player = MinecraftServer.getConnectionManager().getPlayer(uuid);
                                if (player != null) {
                                    player.kick(LoginValidator.NOT_WHITELISTED_MESSAGE);
                                }
                            }
                        } else {
                            sender.sendMessage(uuid + " (" + name + ") is not whitelisted");
                        }
                    });
                });
            }, PLAYER_ARGUMENT);
        }
    }
}
