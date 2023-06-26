package org.phantazm.server.permission;

import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public record PermissionData(@NotNull ConcurrentHashMap<String, CopyOnWriteArraySet<Permission>> groupPermissions,
                             @NotNull ConcurrentHashMap<UUID, CopyOnWriteArraySet<String>> groups) {
}
