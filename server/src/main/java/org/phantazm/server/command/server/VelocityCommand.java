package org.phantazm.server.command.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.phantazm.core.command.CommandUtils;
import org.phantazm.core.command.PermissionLockedCommand;

public class VelocityCommand extends PermissionLockedCommand {
    public enum Type {
        VERTICAL,
        HORIZONTAL,
        BOTH
    }

    public static final Permission PERMISSION = new Permission("admin.velocity");
    private static final Argument<String> PLAYER = ArgumentType.String("player");
    private static final Argument<Type> TYPE = ArgumentType.Enum("type", Type.class)
        .setFormat(ArgumentEnum.Format.LOWER_CASED)
        .setDefaultValue(Type.HORIZONTAL);
    private static final Argument<Integer> AMOUNT = ArgumentType.Integer("amount").setDefaultValue(5);

    public VelocityCommand() {
        super("velo", PERMISSION);

        addConditionalSyntax(CommandUtils.playerSenderCondition(), ((sender, context) -> {
            Player senderPlayer = (Player) sender;

            Player player = MinecraftServer.getConnectionManager().getPlayer(context.get(PLAYER));
            if (player == null) {
                sender.sendMessage(Component.text("That player is not online!", NamedTextColor.RED));
                return;
            }

            int amount = context.get(AMOUNT);
            switch (context.get(TYPE)) {
                case VERTICAL -> player.setVelocity(player.getVelocity().add(0, amount, 0));
                case HORIZONTAL -> {
                    double angle = senderPlayer.getPosition().yaw() * (Math.PI / 180);
                    player.takeKnockback(amount, true, Math.sin(angle), -Math.cos(angle));
                }
                case BOTH -> {
                    double angle = senderPlayer.getPosition().yaw() * (Math.PI / 180);
                    player.takeKnockback(amount, Math.sin(angle), -Math.cos(angle));
                }
            }
        }), PLAYER, TYPE, AMOUNT);
    }
}
