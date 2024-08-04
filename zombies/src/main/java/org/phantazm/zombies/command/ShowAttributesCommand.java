package org.phantazm.zombies.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Iterator;

public class ShowAttributesCommand extends SandboxLockedCommand {
    public static final Permission PERMISSION = new Permission("zombies.playtest.show_attributes");

    public ShowAttributesCommand() {
        super("show_attributes", PERMISSION);
    }

    @Override
    protected void runCommand(@NotNull CommandContext context, @NotNull ZombiesScene scene, @NotNull Player sender) {
        scene.setLegit(false);

        for (AttributeInstance entry : sender.attributeInstances()) {
            Attribute attribute = entry.getAttribute();

            sender.sendMessage(Component.text(attribute.key() + ":" + entry.getBaseValue(), NamedTextColor.GRAY));
            Iterator<AttributeModifier> modifierIterator = entry.getModifiers().iterator();
            while (modifierIterator.hasNext()) {
                AttributeModifier modifier = modifierIterator.next();
                sender.sendMessage(Component.text("  - amount: " + modifier.getAmount(), NamedTextColor.DARK_GRAY));
                sender.sendMessage(Component.text("  - operation: " + modifier.getOperation(), NamedTextColor.DARK_GRAY));
                if (modifierIterator.hasNext()) {
                    sender.sendMessage("");
                }
            }
        }
    }
}
