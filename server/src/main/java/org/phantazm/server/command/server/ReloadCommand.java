package org.phantazm.server.command.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.permission.Permission;
import org.phantazm.core.command.PermissionLockedCommand;
import org.phantazm.server.LobbyFeature;
import org.phantazm.server.MobFeature;
import org.phantazm.server.ZombiesFeature;

import java.io.IOException;

public class ReloadCommand extends PermissionLockedCommand {
    public static final Permission PERMISSION = new Permission("admin.reload");

    public enum ReloadTarget {
        ZOMBIES_MAPS,
        MOBS,
        LOBBIES,
        ALL
    }

    private static final Argument<ReloadTarget> RELOAD_ARGUMENT = ArgumentType.Enum("reload-target", ReloadTarget.class)
        .setFormat(ArgumentEnum.Format.LOWER_CASED).setDefaultValue(ReloadTarget.ALL);

    public ReloadCommand() {
        super("reload", PERMISSION);

        addSyntax((sender, context) -> {
            try {
                switch (context.get(RELOAD_ARGUMENT)) {
                    case ZOMBIES_MAPS -> ZombiesFeature.reload();
                    case MOBS -> MobFeature.reload();
                    case LOBBIES -> LobbyFeature.reload();
                    case ALL -> {
                        MobFeature.reload();
                        ZombiesFeature.reload();
                        LobbyFeature.reload();
                    }
                }

                sender.sendMessage(Component.text("Reloaded configs!", NamedTextColor.GREEN));
            } catch (IOException e) {
                sender.sendMessage(Component.text("An exception occurred while reloading configs; please see " +
                    "the server log for details.", NamedTextColor.RED));
                LOGGER.warn("IOException when reloading configs", e);
            }
        }, RELOAD_ARGUMENT);
    }
}