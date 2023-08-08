package org.phantazm.server.role;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;
import org.jooq.Result;
import org.phantazm.server.permission.PermissionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static org.jooq.impl.DSL.*;

public class BasicRoleStore implements RoleStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicRoleStore.class);

    private final DataSource dataSource;
    private final Executor executor;
    private final Cache<UUID, Set<Role>> roleCache;
    private final Supplier<PermissionHandler> permissionHandler;

    private final Map<String, Role> roleMap;

    public BasicRoleStore(@NotNull DataSource dataSource, @NotNull Executor executor,
            @NotNull Supplier<PermissionHandler> permissionHandler) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.roleCache = Caffeine.newBuilder().maximumSize(1024).expireAfterAccess(Duration.ofMinutes(5)).build();
        this.permissionHandler = Objects.requireNonNull(permissionHandler, "permissionHandler");

        this.roleMap = new ConcurrentHashMap<>();
    }

    @Override
    public void register(@NotNull Role role) {
        roleMap.put(role.identifier(), role);
    }

    @Override
    public @NotNull Role getStylingRole(@NotNull Player player) {
        Set<Role> roles = roleCache.get(player.getUuid(), this::loadRoles);

        Role stylingRole = Role.NONE;
        int highestPriority = Integer.MIN_VALUE;
        for (Role role : roles) {
            if (role.priority() >= highestPriority) {
                stylingRole = role;
                highestPriority = role.priority();
            }
        }

        return stylingRole;
    }

    private void applyRoles0(Player player, Set<Role> roles) {
        for (Role role : roles) {
            player.getAllPermissions().addAll(role.grantedPermissions());
        }
    }

    @Override
    public void applyRoles(@NotNull Player player) {
        Set<Role> roles = this.roleCache.get(player.getUuid(), this::loadRoles);
        applyRoles0(player, roles);
    }

    @Override
    public @NotNull Optional<Role> role(@NotNull String identifier) {
        return Optional.ofNullable(roleMap.get(identifier));
    }

    @Override
    public void giveRole(@NotNull UUID uuid, @NotNull String identifier) {
        Role role = roleMap.get(identifier);
        if (role == null) {
            return;
        }

        this.roleCache.get(uuid, this::loadRoles).add(role);
        executor.execute(() -> {
            try (Connection connection = dataSource.getConnection()) {
                using(connection).insertInto(table("player_roles"), field("player_uuid"), field("player_role"))
                        .values(uuid, identifier).onDuplicateKeyUpdate().set(field("player_uuid"), uuid)
                        .set(field("player_role"), identifier).execute();
            }
            catch (SQLException e) {
                LOGGER.warn("SQLException when writing role update to database", e);
            }

            Player player = MinecraftServer.getConnectionManager().getPlayer(uuid);
            if (player == null) {
                return;
            }

            player.getAllPermissions().addAll(role.grantedPermissions());
        });
    }

    @Override
    public void removeRole(@NotNull UUID uuid, @NotNull String identifier) {
        Role role = roleMap.get(identifier);
        if (role == null) {
            return;
        }

        Set<Role> roles = this.roleCache.get(uuid, this::loadRoles);
        roles.remove(role);

        executor.execute(() -> {
            try (Connection connection = dataSource.getConnection()) {
                using(connection).deleteFrom(table("player_roles"))
                        .where(field("player_uuid").eq(uuid).and(field("player_role").eq(identifier))).execute();
            }
            catch (SQLException e) {
                LOGGER.warn("SQLException when deleting player role from database", e);
            }

            Player player = MinecraftServer.getConnectionManager().getPlayer(uuid);
            if (player == null) {
                return;
            }

            player.getAllPermissions().clear();

            //re-apply permissions that come from permission groups
            permissionHandler.get().applyPermissions(uuid, player);

            //re-apply roles
            applyRoles0(player, roles);
        });
    }

    private Set<Role> loadRoles(UUID key) {
        try (Connection connection = dataSource.getConnection()) {
            Result<Record> result =
                    using(connection).selectFrom(table("player_roles")).where(field("player_uuid").eq(key)).fetch();

            Set<Role> roleSet = new HashSet<>(result.size());
            for (Record record : result) {
                String playerRole = (String)record.get("player_role");
                if (playerRole == null) {
                    continue;
                }

                Role role = roleMap.get(playerRole);
                if (role == null) {
                    continue;
                }

                roleSet.add(role);
            }

            return new CopyOnWriteArraySet<>(roleSet);
        }
        catch (Exception e) {
            LOGGER.warn("Exception when fetching player roles", e);
        }

        return new CopyOnWriteArraySet<>();
    }
}
