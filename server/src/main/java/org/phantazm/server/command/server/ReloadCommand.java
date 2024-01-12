package org.phantazm.server.command.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.permission.Permission;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.server.MobFeature;

import java.io.IOException;

public class ReloadCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.reload");

    public ReloadCommand() {
        super("reload", PERMISSION);

        addSyntax((sender, context) -> {
            try {
                MobFeature.reload();
                sender.sendMessage("Reloaded configs!");
            } catch (IOException e) {
                sender.sendMessage(Component.text("An exception occurred while reloading configs; please see " +
                    "the server log for details.", NamedTextColor.RED));
            }
        });
    }
}