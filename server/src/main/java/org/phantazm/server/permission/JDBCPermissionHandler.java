package org.phantazm.server.permission;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.role.Role;
import org.phantazm.core.role.RoleStore;
import org.phantazm.stats.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;

public class JDBCPermissionHandler implements PermissionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCPermissionHandler.class);

    private final Cache<UUID, Set<String>> playerPermissionGroupCache;
    private final Cache<String, Set<Permission>> groupPermissionCache;
    private final DataSource dataSource;
    private final Executor executor;
    private final RoleStore roleStore;

    public JDBCPermissionHandler(@NotNull DataSource dataSource, @NotNull Executor executor,
        @NotNull RoleStore roleStore) {
        this.playerPermissionGroupCache =
            Caffeine.newBuilder().softValues().maximumSize(1024).expireAfterAccess(Duration.ofMinutes(5)).build();
        this.groupPermissionCache =
            Caffeine.newBuilder().softValues().maximumSize(1024).expireAfterAccess(Duration.ofMinutes(5)).build();
        this.dataSource = Objects.requireNonNull(dataSource);
        this.executor = Objects.requireNonNull(executor);
        this.roleStore = Objects.requireNonNull(roleStore);
    }

    @Override
    public void initTables() {
        DatabaseUtils.runSql(LOGGER, "initTables", dataSource, (connection, statement) -> {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS permission_groups (
                    permission_group VARCHAR(64) NOT NULL,
                    permission VARCHAR(64) NOT NULL,
                    
                    PRIMARY KEY (permission_group, permission)
                );
                """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS player_permission_groups (
                    player_uuid UUID NOT NULL,
                    player_group VARCHAR(64) NOT NULL,
                    
                    PRIMARY KEY (player_uuid, player_group),
                    CONSTRAINT fk_player_group
                        FOREIGN KEY (player_group) REFERENCES permission_groups (permission_group)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE
                );
                """);
        });
    }

    @Override
    public void applyPermissions(@NotNull Player target) {
        Objects.requireNonNull(target);
        executor.execute(() -> {
            applyPermissions0(target.getUuid(), target);
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
            String permissionName = permission.getPermissionName();
            DatabaseUtils.runPreparedSql(LOGGER, "addGroupPermission", dataSource, """
                INSERT INTO permission_groups (permission_group, permission)
                VALUES(?, ?)
                ON DUPLICATE KEY UPDATE permission=?
                """, (connection, statement) -> {
                statement.setString(1, group);
                statement.setString(2, permissionName);
                statement.setString(3, permissionName);

                statement.execute();
            });

            applyAll0();
        });
    }

    @Override
    public void removeGroupPermission(@NotNull String group, @NotNull Permission permission) {
        groupPermissionCache.get(group, ignored -> new CopyOnWriteArraySet<>()).remove(permission);

        executor.execute(() -> {
            String permissionName = permission.getPermissionName();
            DatabaseUtils.runPreparedSql(LOGGER, "removeGroupPermission", dataSource, """
                DELETE FROM permission_groups
                WHERE (permission_group, permission) = (?, ?)
                """, (connection, statement) -> {
                statement.setString(1, group);
                statement.setString(2, permissionName);

                statement.execute();
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
            DatabaseUtils.runPreparedSql(LOGGER, "addToGroup", dataSource, """
                INSERT INTO player_permission_groups (player_uuid, player_group)
                VALUES(?, ?)
                ON DUPLICATE KEY UPDATE player_group=?
                """, (connection, statement) -> {
                statement.setString(1, uuid.toString());
                statement.setString(2, group);
                statement.setString(3, group);

                statement.execute();
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
            DatabaseUtils.runPreparedSql(LOGGER, "removeFromGroup", dataSource, """
                DELETE FROM player_permission_groups
                WHERE (player_uuid, player_group) = (?, ?)
                """, (connection, statement) -> {
                statement.setString(1, uuid.toString());
                statement.setString(2, group);

                statement.execute();
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

    private Set<String> readPermissionGroups(UUID uuid) {
        return DatabaseUtils.runPreparedSql(LOGGER, "readPermissionGroups", CopyOnWriteArraySet::new, dataSource, """
            SELECT player_group FROM player_permission_groups
            WHERE player_uuid=?
            """, (connection, statement) -> {
            statement.setString(1, uuid.toString());

            ResultSet result = statement.executeQuery();

            if (!result.next()) {
                return new CopyOnWriteArraySet<>();
            }

            List<String> temp = new ArrayList<>(5);
            do {
                temp.add(result.getString(1));
            }
            while (result.next());

            return new CopyOnWriteArraySet<>(temp);
        });
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
            return DatabaseUtils.runPreparedSql(LOGGER, "applyGroup", CopyOnWriteArraySet::new, dataSource,
                """
                    SELECT permission FROM permission_groups
                    WHERE permission_group=?
                    """, (connection, statement) -> {
                    statement.setString(1, group);
                    ResultSet result = statement.executeQuery();

                    if (!result.next()) {
                        return new CopyOnWriteArraySet<>();
                    }

                    List<Permission> temp = new ArrayList<>(5);
                    do {
                        temp.add(new Permission(result.getString(1)));
                    }
                    while (result.next());

                    return new CopyOnWriteArraySet<>(temp);
                });
        });

        commandSender.addPermissions(permissions);
    }

    private void applyOptionalPlayer(UUID uuid) {
        Player target = MinecraftServer.getConnectionManager().getPlayer(uuid);
        if (target != null) {
            applyPermissions0(uuid, target);
        }
    }
}
