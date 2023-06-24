package org.phantazm.server.command.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.server.player.LoginValidator;

public class WhitelistCommand extends Command {
    public static final Permission PERMISSION = new Permission("admin.whitelist");
    private static final ArgumentWord PLAYER_ARGUMENT = ArgumentType.Word("player");

    public WhitelistCommand(@NotNull IdentitySource identitySource, @NotNull LoginValidator loginValidator,
            boolean whitelist) {
        super("whitelist");

        addSubcommand(new Add(identitySource, loginValidator));
        addSubcommand(new Remove(identitySource, loginValidator, whitelist));
    }

    private static class Add extends Command {
        private Add(@NotNull IdentitySource identitySource, @NotNull LoginValidator loginValidator) {
            super("add");

            setCondition((sender, commandString) -> sender.hasPermission(PERMISSION));
            addConditionalSyntax(getCondition(), (sender, context) -> {
                String name = context.get(PLAYER_ARGUMENT);
                identitySource.getUUID(name).whenComplete((uuidOptional, throwable) -> {
                    uuidOptional.ifPresent(uuid -> {
                        if (loginValidator.isWhitelisted(uuid)) {
                            sender.sendMessage(uuid + " (" + name + ") is already whitelisted");
                        }
                        else {
                            loginValidator.addWhitelist(uuid);
                            sender.sendMessage("Whitelisted " + uuid + " (" + name + ")");
                        }
                    });
                });
            }, PLAYER_ARGUMENT);
        }
    }

    private static class Remove extends Command {
        private Remove(@NotNull IdentitySource identitySource, @NotNull LoginValidator loginValidator,
                boolean whitelist) {
            super("remove");

            setCondition((sender, commandString) -> sender.hasPermission(PERMISSION));
            addConditionalSyntax(getCondition(), (sender, context) -> {
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
                        }
                        else {
                            sender.sendMessage(uuid + " (" + name + ") is not whitelisted");
                        }
                    });
                });
            }, PLAYER_ARGUMENT);
        }
    }
}
