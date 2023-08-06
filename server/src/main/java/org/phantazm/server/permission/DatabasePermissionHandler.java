package org.phantazm.server.permission;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.command.CommandSender;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

public class DatabasePermissionHandler implements PermissionHandler {
    private final Cache<UUID, Set<Permission>> playerPermissionCache;

    public DatabasePermissionHandler() {
        this.playerPermissionCache =
                Caffeine.newBuilder().softValues().maximumSize(2048).expireAfterAccess(Duration.ofMinutes(5)).build();
    }

    @Override
    public void applyPermissions(@NotNull UUID uuid, @NotNull CommandSender sender) {
        
    }

    @Override
    public void flush() {

    }

    @Override
    public void reload() {

    }

    @Override
    public void addGroupPermission(@NotNull String group, @NotNull Permission permission) {

    }

    @Override
    public void removeGroupPermission(@NotNull String group, @NotNull Permission permission) {

    }

    @Override
    public void addToGroup(@NotNull UUID uuid, @NotNull String group) {

    }

    @Override
    public void removeFromGroup(@NotNull UUID uuid, @NotNull String group) {

    }
}
