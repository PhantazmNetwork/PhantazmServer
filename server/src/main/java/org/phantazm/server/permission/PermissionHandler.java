package org.phantazm.server.permission;

import net.minestom.server.command.CommandSender;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PermissionHandler {
    String EVERYONE_GROUP = "everyone";

    void applyPermissions(@NotNull UUID uuid, @NotNull CommandSender sender);

    void reload();

    void addGroupPermission(@NotNull String group, @NotNull Permission permission);

    void removeGroupPermission(@NotNull String group, @NotNull Permission permission);

    void addToGroup(@NotNull UUID uuid, @NotNull String group);

    void removeFromGroup(@NotNull UUID uuid, @NotNull String group);

    @NotNull CompletableFuture<@Unmodifiable Set<String>> groups(@NotNull UUID uuid);
}
