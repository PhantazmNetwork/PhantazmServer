package org.phantazm.server.permission;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.steanky.toolkit.function.ThrowingRunnable;
import com.github.steanky.toolkit.function.ThrowingSupplier;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jooq.Record;
import org.jooq.Result;
import org.phantazm.server.role.Role;
import org.phantazm.server.role.RoleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.jooq.impl.DSL.*;

public class DatabasePermissionHandler implements PermissionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabasePermissionHandler.class);

    private final Cache<UUID, Set<String>> playerPermissionGroupCache;
    private final Cache<String, Set<Permission>> groupPermissionCache;
    private final DataSource dataSource;
    private final Executor executor;
    private final RoleStore roleStore;

    public DatabasePermissionHandler(@NotNull DataSource dataSource, @NotNull Executor executor,
            @NotNull RoleStore roleStore) {
        this.playerPermissionGroupCache =
                Caffeine.newBuilder().softValues().maximumSize(1024).expireAfterAccess(Duration.ofMinutes(5)).build();
        this.groupPermissionCache =
                Caffeine.newBuilder().softValues().maximumSize(1024).expireAfterAccess(Duration.ofMinutes(5)).build();
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.roleStore = Objects.requireNonNull(roleStore, "roleStore");
    }

    @Override
    public void applyPermissions(@NotNull UUID uuid, @NotNull CommandSender sender) {
        executor.execute(() -> {
            applyPermissions0(uuid, sender);
        });
    }

    @Override
    public void reload() {
        executor.execute(this::applyAll0);
    }

    @Override
    public void addGroupPermission(@NotNull String group, @NotNull Permission permission) {
        groupPermissionCache.get(group, ignored -> new CopyOnWriteArraySet<>()).add(permission);

        executor.execute(() -> {
            write(() -> {
                String permissionName = permission.getPermissionName();
                try (Connection connection = dataSource.getConnection()) {
                    using(connection).insertInto(table("permission_groups"), field("permission_group"),
                                    field("permission")).values(group, permissionName).onDuplicateKeyUpdate()
                            .set(field("permission"), permissionName).execute();
                }
            });

            applyAll0();
        });
    }

    @Override
    public void removeGroupPermission(@NotNull String group, @NotNull Permission permission) {
        groupPermissionCache.get(group, ignored -> new CopyOnWriteArraySet<>()).remove(permission);

        executor.execute(() -> {
            write(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    using(connection).deleteFrom(table("permission_groups")).where(field("permission_group").eq(group)
                            .and(field("permission").eq(permission.getPermissionName()))).execute();
                }
            });

            applyAll0();
        });
    }

    @Override
    public void addToGroup(@NotNull UUID uuid, @NotNull String group) {
        if (group.equals(EVERYONE_GROUP)) {
            return;
        }

        playerPermissionGroupCache.get(uuid, ignored -> new CopyOnWriteArraySet<>()).add(group);

        executor.execute(() -> {
            write(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    using(connection).insertInto(table("player_permission_groups"), field("player_uuid"),
                                    field("player_group")).values(uuid, group).onDuplicateKeyUpdate()
                            .set(field("player_group"), group).execute();
                }
            });

            applyOptionalPlayer(uuid);
        });
    }

    @Override
    public void removeFromGroup(@NotNull UUID uuid, @NotNull String group) {
        if (group.equals(EVERYONE_GROUP)) {
            return;
        }

        playerPermissionGroupCache.get(uuid, ignored -> new CopyOnWriteArraySet<>()).remove(group);

        executor.execute(() -> {
            write(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    using(connection).deleteFrom(table("player_permission_groups"))
                            .where(field("player_uuid").eq(uuid).and(field("player_group").eq(group))).execute();
                }
            });

            applyOptionalPlayer(uuid);
        });
    }

    @Override
    public @NotNull CompletableFuture<@Unmodifiable Set<String>> groups(@NotNull UUID uuid) {
        Set<String> groups = this.playerPermissionGroupCache.getIfPresent(uuid);
        if (groups != null) {
            return CompletableFuture.completedFuture(groups);
        }

        return CompletableFuture.supplyAsync(() -> {
            return this.playerPermissionGroupCache.get(uuid, this::readPermissionGroups);
        }, executor);
    }

    private static void write(ThrowingRunnable<SQLException> supplier) {
        try {
            supplier.run();
        }
        catch (SQLException e) {
            LOGGER.warn("Exception when writing to permission database", e);
        }
    }

    private static <T> T read(ThrowingSupplier<? extends Result<Record>, ? extends SQLException> reader,
            Function<? super Result<Record>, ? extends T> mapper, Supplier<? extends T> defaultValue) {
        try {
            return mapper.apply(reader.get());
        }
        catch (SQLException e) {
            LOGGER.warn("Exception when querying permission database", e);
        }

        return defaultValue.get();
    }

    private Set<String> readPermissionGroups(UUID uuid) {
        return read(() -> {
            try (Connection connection = dataSource.getConnection()) {
                return using(connection).selectFrom(table("player_permission_groups"))
                        .where(field("player_uuid").eq(uuid)).fetch();
            }
        }, DatabasePermissionHandler::groupsFromResult, CopyOnWriteArraySet::new);
    }

    private void applyPermissions0(UUID uuid, CommandSender sender) {
        sender.clearPermissions();

        Set<String> permissionGroups = playerPermissionGroupCache.get(uuid, this::readPermissionGroups);

        for (String group : permissionGroups) {
            applyGroup(group, sender);
        }

        applyGroup(EVERYONE_GROUP, sender);

        roleStore.roles(uuid).thenAccept(roles -> {
            for (Role role : roles) {
                sender.addPermissions(role.grantedPermissions());
            }
        }).whenComplete((ignored, error) -> {
            if (sender instanceof Player player) {
                player.sendPacket(MinecraftServer.getCommandManager().createDeclareCommandsPacket(player));
            }
        });
    }

    private void applyAll0() {
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            applyPermissions0(player.getUuid(), player);
        }
    }

    private void applyGroup(String group, CommandSender commandSender) {
        Set<Permission> permissions = groupPermissionCache.get(group, key -> {
            return read(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    return using(connection).selectFrom(table("permission_groups"))
                            .where(field("permission_group").eq(key)).fetch();
                }
            }, DatabasePermissionHandler::permissionsFromResult, CopyOnWriteArraySet::new);
        });

        for (Permission permission : permissions) {
            commandSender.addPermission(permission);
        }
    }

    private void applyOptionalPlayer(UUID uuid) {
        Player target = MinecraftServer.getConnectionManager().getPlayer(uuid);
        if (target != null) {
            applyPermissions0(uuid, target);
        }
    }

    private static Set<String> groupsFromResult(Result<Record> result) {
        if (result == null) {
            return new CopyOnWriteArraySet<>();
        }

        List<String> temp = new ArrayList<>(result.size());
        for (Record record : result) {
            String group = (String)record.get("player_group");
            if (group == null) {
                continue;
            }

            temp.add(group);
        }

        return new CopyOnWriteArraySet<>(temp);
    }

    private static Set<Permission> permissionsFromResult(Result<Record> result) {
        if (result == null) {
            return new CopyOnWriteArraySet<>();
        }

        List<Permission> temp = new ArrayList<>(result.size());
        for (Record record : result) {
            String permission = (String)record.get("permission");
            if (permission == null) {
                continue;
            }

            temp.add(new Permission(permission));
        }

        return new CopyOnWriteArraySet<>(temp);
    }
}
