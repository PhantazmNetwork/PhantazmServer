package org.phantazm.server.permission;

import net.minestom.server.command.CommandSender;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface PermissionHandler {
    String EVERYONE_GROUP = "everyone";

    void applyPermissions(@NotNull UUID uuid, @NotNull CommandSender sender);

    void flush();

    void reload();

    void addGroupPermission(@NotNull String group, @NotNull Permission permission);

    void removeGroupPermission(@NotNull String group, @NotNull Permission permission);

    void addToGroup(@NotNull UUID uuid, @NotNull String group);

    void removeFromGroup(@NotNull UUID uuid, @NotNull String group);
}
