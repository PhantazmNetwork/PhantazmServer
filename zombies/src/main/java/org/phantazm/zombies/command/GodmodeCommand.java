package org.phantazm.zombies.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.phantazm.zombies.Tags;

public class GodmodeCommand extends Command {
    public GodmodeCommand() {
        super("godmode");

        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("You have to be a player to use that command!", NamedTextColor.RED));
                return;
            }

            boolean godmode = player.getTag(Tags.INVULNERABILITY_TAG);
            player.setTag(Tags.INVULNERABILITY_TAG, !godmode);

            if (godmode) {
                sender.sendMessage("Disabled godmode.");
            }
            else {
                sender.sendMessage("Enabled godmode.");
            }
        });
    }
}
