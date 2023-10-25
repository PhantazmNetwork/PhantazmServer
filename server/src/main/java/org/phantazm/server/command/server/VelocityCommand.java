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

import java.util.UUID;

public class VelocityCommand extends PermissionLockedCommand {
    private static final UUID PERSON_WHO_SHOULDNT_USE_VELO =
        UUID.fromString("7825e26a-df0b-4391-baf4-4c9ce151293d");

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
            if (senderPlayer.getUuid().equals(PERSON_WHO_SHOULDNT_USE_VELO)) {
                senderPlayer.sendMessage(Component.text(":tr:", NamedTextColor.RED));
                senderPlayer.kill();
                return;
            }

            Player player = MinecraftServer.getConnectionManager().getPlayer(context.get(PLAYER));
            if (player == null) {
                sender.sendMessage(Component.text("That player is not online!", NamedTextColor.RED));
                return;
            }

            int amount = context.get(AMOUNT);
            if (Math.abs(amount) > 100) {
                sender.sendMessage(Component.text("That value is too large!", NamedTextColor.RED));
                return;
            }

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
