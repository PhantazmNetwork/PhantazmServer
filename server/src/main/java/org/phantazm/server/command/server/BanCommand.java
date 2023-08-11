package org.phantazm.server.command.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.server.validator.LoginValidator;

public class BanCommand extends Command {
    public static final Permission PERMISSION = new Permission("admin.ban");

    private static final ArgumentWord PLAYER_ARGUMENT = ArgumentType.Word("player");
    private static final Argument<String[]> REASON = ArgumentType.StringArray("reason").setDefaultValue(new String[0]);

    public BanCommand(@NotNull IdentitySource identitySource, @NotNull LoginValidator loginValidator) {
        super("ban");

        setCondition((sender, commandString) -> sender.hasPermission(PERMISSION));
        addConditionalSyntax(getCondition(), (sender, context) -> {
            String name = context.get(PLAYER_ARGUMENT);
            identitySource.getUUID(name).whenComplete((uuidOptional, throwable) -> {
                uuidOptional.ifPresent(uuid -> {
                    Component reason = MiniMessage.miniMessage().deserialize(String.join(" ", context.get(REASON)));
                    loginValidator.ban(uuid, reason);

                    Player player = MinecraftServer.getConnectionManager().getPlayer(uuid);
                    if (player != null) {
                        player.kick(reason);
                    }

                    sender.sendMessage("Banned " + uuid + " (" + name + ")");
                });
            });
        }, PLAYER_ARGUMENT, REASON);
    }
}
