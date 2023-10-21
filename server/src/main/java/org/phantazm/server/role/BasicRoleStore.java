package org.phantazm.server.role;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jooq.Record;
import org.jooq.Result;
import org.phantazm.commons.FutureUtils;
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

import static org.jooq.impl.DSL.*;

public class BasicRoleStore implements RoleStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicRoleStore.class);

    private final DataSource dataSource;
    private final Executor executor;
    private final Cache<UUID, Set<Role>> roleCache;

    private final Map<String, Role> roleMap;

    public BasicRoleStore(@NotNull DataSource dataSource, @NotNull Executor executor) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.executor = Objects.requireNonNull(executor);
        this.roleCache = Caffeine.newBuilder().maximumSize(1024).expireAfterAccess(Duration.ofMinutes(5)).build();

        this.roleMap = new ConcurrentHashMap<>();
    }

    @Override
    public void register(@NotNull Role role) {
        if (role.identifier().equals(Role.NONE.identifier())) {
            return;
        }

        roleMap.put(role.identifier(), role);
    }

    @Override
    public @NotNull CompletableFuture<Role> getStylingRole(@NotNull UUID uuid) {
        Set<Role> currentRoles = roleCache.getIfPresent(uuid);
        if (currentRoles != null) {
            //avoid async call if we can
            return CompletableFuture.completedFuture(getStylingRole0(currentRoles));
        }

        return CompletableFuture.supplyAsync(() -> {
            return getStylingRole0(roleCache.get(uuid, this::loadRoles));
        }, executor);
    }

    @Override
    public @NotNull Optional<Role> role(@NotNull String identifier) {
        return Optional.ofNullable(roleMap.get(identifier));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> giveRole(@NotNull UUID uuid, @NotNull String identifier) {
        if (identifier.equals(RoleStore.DEFAULT) || identifier.equals(Role.NONE.identifier())) {
            return FutureUtils.falseCompletedFuture();
        }

        Role role = roleMap.get(identifier);
        if (role == null) {
            return FutureUtils.falseCompletedFuture();
        }

        return CompletableFuture.supplyAsync(() -> {
            Set<Role> roles = this.roleCache.get(uuid, this::loadRoles);
            boolean added = roles.add(role);

            try (Connection connection = dataSource.getConnection()) {
                using(connection).insertInto(table("player_roles"), field("player_uuid"), field("player_role"))
                    .values(uuid, identifier).onDuplicateKeyUpdate().set(field("player_uuid"), uuid)
                    .set(field("player_role"), identifier).execute();
            } catch (SQLException e) {
                LOGGER.warn("SQLException when writing role update to database", e);
            }

            if (added) {
                styleIfPresent(uuid, roles);
            }

            return added;
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> removeRole(@NotNull UUID uuid, @NotNull String identifier) {
        if (identifier.equals(RoleStore.DEFAULT) || identifier.equals(Role.NONE.identifier())) {
            return FutureUtils.falseCompletedFuture();
        }

        Role role = roleMap.get(identifier);
        if (role == null) {
            return FutureUtils.falseCompletedFuture();
        }

        return CompletableFuture.supplyAsync(() -> {
            Set<Role> roles = this.roleCache.get(uuid, this::loadRoles);
            boolean removed = roles.remove(role);
            try (Connection connection = dataSource.getConnection()) {
                using(connection).deleteFrom(table("player_roles"))
                    .where(field("player_uuid").eq(uuid).and(field("player_role").eq(identifier))).execute();
            } catch (SQLException e) {
                LOGGER.warn("SQLException when deleting player role from database", e);
            }

            if (removed) {
                styleIfPresent(uuid, roles);
            }

            return removed;
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<@Unmodifiable Set<@NotNull Role>> roles(@NotNull UUID uuid) {
        Set<Role> roles = this.roleCache.getIfPresent(uuid);
        if (roles != null) {
            return CompletableFuture.completedFuture(roles);
        }

        return CompletableFuture.supplyAsync(() -> {
            return this.roleCache.get(uuid, this::loadRoles);
        }, executor);
    }

    private void styleIfPresent(UUID uuid, Set<Role> roles) {
        Player player = MinecraftServer.getConnectionManager().getPlayer(uuid);
        if (player == null) {
            return;
        }

        getStylingRole0(roles).styleDisplayName(player);
    }

    private Role getStylingRole0(Set<Role> roles) {
        Role stylingRole = roleMap.getOrDefault(RoleStore.DEFAULT, Role.NONE);
        int highestPriority = Integer.MIN_VALUE;
        for (Role role : roles) {
            if (role.priority() >= highestPriority) {
                stylingRole = role;
                highestPriority = role.priority();
            }
        }

        return stylingRole;
    }

    private Set<Role> loadRoles(UUID key) {
        Role defaultRole = roleMap.get(RoleStore.DEFAULT);

        try (Connection connection = dataSource.getConnection()) {
            Result<Record> result =
                using(connection).selectFrom(table("player_roles")).where(field("player_uuid").eq(key)).fetch();

            Set<Role> roleSet = new HashSet<>(result.size() + (defaultRole == null ? 0 : 1));
            for (Record record : result) {
                String playerRole = (String) record.get("player_role");
                if (playerRole == null) {
                    continue;
                }

                Role role = roleMap.get(playerRole);
                if (role == null) {
                    continue;
                }

                roleSet.add(role);
            }

            if (defaultRole != null) {
                roleSet.add(defaultRole);
            }

            return new CopyOnWriteArraySet<>(roleSet);
        } catch (Exception e) {
            LOGGER.warn("Exception when fetching player roles", e);
        }

        return defaultRole == null ? new CopyOnWriteArraySet<>() : new CopyOnWriteArraySet<>(List.of(defaultRole));
    }
}
