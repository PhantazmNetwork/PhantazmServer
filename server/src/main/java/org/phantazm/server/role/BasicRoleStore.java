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
import java.util.concurrent.CompletableFuture;
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
    public @NotNull CompletableFuture<Role> getStylingRole(@NotNull Player player) {
        Set<Role> currentRole = roleCache.getIfPresent(player.getUuid());
        if (currentRole != null) {
            //avoid async call if we can
            return CompletableFuture.completedFuture(getStylingRole0(currentRole));
        }

        return CompletableFuture.supplyAsync(() -> {
            Set<Role> roles = roleCache.get(player.getUuid(), this::loadRoles);
            return getStylingRole0(roles);
        }, executor);
    }

    private Role getStylingRole0(Set<Role> roles) {
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

        player.sendPacket(MinecraftServer.getCommandManager().createDeclareCommandsPacket(player));
    }

    @Override
    public void applyRoles(@NotNull Player player) {
        Set<Role> roles = this.roleCache.getIfPresent(player.getUuid());
        if (roles != null) {
            applyRoles0(player, roles);
            return;
        }

        executor.execute(() -> {
            applyRoles0(player, this.roleCache.get(player.getUuid(), this::loadRoles));
        });
    }

    @Override
    public @NotNull Optional<Role> role(@NotNull String identifier) {
        return Optional.ofNullable(roleMap.get(identifier));
    }

    @Override
    public boolean giveRole(@NotNull UUID uuid, @NotNull String identifier) {
        Role role = roleMap.get(identifier);
        if (role == null) {
            return false;
        }

        boolean added = this.roleCache.get(uuid, this::loadRoles).add(role);
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

        return added;
    }

    @Override
    public boolean removeRole(@NotNull UUID uuid, @NotNull String identifier) {
        Role role = roleMap.get(identifier);
        if (role == null) {
            return false;
        }

        Set<Role> roles = this.roleCache.get(uuid, this::loadRoles);
        boolean removed = roles.remove(role);

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

        return removed;
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
