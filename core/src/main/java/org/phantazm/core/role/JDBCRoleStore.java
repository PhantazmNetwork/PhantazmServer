package org.phantazm.core.role;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.FutureUtils;
import org.phantazm.stats.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;

public class JDBCRoleStore implements RoleStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCRoleStore.class);

    private final DataSource dataSource;
    private final Executor executor;
    private final Cache<UUID, Set<Role>> roleCache;

    private final Map<String, Role> roleMap;

    public JDBCRoleStore(@NotNull DataSource dataSource, @NotNull Executor executor) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.executor = Objects.requireNonNull(executor);
        this.roleCache = Caffeine.newBuilder().maximumSize(1024).expireAfterAccess(Duration.ofMinutes(5)).build();

        this.roleMap = new ConcurrentHashMap<>();
    }

    @Override
    public void initTables() {
        Utils.runSql(LOGGER, "initTables", dataSource, (connection, statement) -> {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS player_roles (
                    player_uuid UUID NOT NULL,
                    player_role VARCHAR(64) NOT NULL,
                    CONSTRAINT unique_role
                        unique(player_uuid, player_role)
                );
                """);
        });
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
            return FutureUtils.completedFuture(getStylingRole0(currentRoles));
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
        if (identifier.equals(DEFAULT) || identifier.equals(Role.NONE.identifier())) {
            return FutureUtils.falseCompletedFuture();
        }

        Role role = roleMap.get(identifier);
        if (role == null) {
            return FutureUtils.falseCompletedFuture();
        }

        return CompletableFuture.supplyAsync(() -> {
            Set<Role> roles = this.roleCache.get(uuid, this::loadRoles);
            boolean added = roles.add(role);

            Utils.runPreparedSql(LOGGER, "giveRole", dataSource, """
                INSERT INTO player_roles (player_uuid, player_role) VALUES (?, ?)
                ON DUPLICATE KEY UPDATE player_uuid=?, player_role=?
                """, (connection, statement) -> {
                String uuidString = uuid.toString();
                statement.setString(1, uuidString);
                statement.setString(2, identifier);

                statement.setString(3, uuidString);
                statement.setString(4, identifier);

                statement.execute();
            });

            if (added) {
                styleIfPresent(uuid, roles);
            }

            return added;
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> removeRole(@NotNull UUID uuid, @NotNull String identifier) {
        if (identifier.equals(DEFAULT) || identifier.equals(Role.NONE.identifier())) {
            return FutureUtils.falseCompletedFuture();
        }

        Role role = roleMap.get(identifier);
        if (role == null) {
            return FutureUtils.falseCompletedFuture();
        }

        return CompletableFuture.supplyAsync(() -> {
            Set<Role> roles = this.roleCache.get(uuid, this::loadRoles);
            boolean removed = roles.remove(role);

            Utils.runPreparedSql(LOGGER, "removeRole", dataSource, """
                DELETE FROM player_roles
                WHERE (player_uuid, player_role) = (?, ?)
                """, (connection, statement) -> {
                statement.setString(1, uuid.toString());
                statement.setString(2, identifier);

                statement.execute();
            });

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
        Role stylingRole = roleMap.getOrDefault(DEFAULT, Role.NONE);
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
        Role defaultRole = roleMap.get(DEFAULT);

        Set<String> roles = Utils.runPreparedSql(LOGGER, "loadRoles", new CopyOnWriteArraySet<>(),
            dataSource, """
                SELECT player_role FROM player_roles
                WHERE player_uuid = ?
                """, (connection, statement) -> {
                statement.setString(1, key.toString());

                ResultSet result = statement.executeQuery();

                if (!result.next()) {
                    return Set.of();
                }

                Set<String> playerRoles = new HashSet<>();
                do {
                    playerRoles.add(result.getString(1));
                }
                while (result.next());

                return playerRoles;
            });

        Set<Role> roleSet = new HashSet<>(roles.size());
        for (String roleName : roles) {
            Role role = roleMap.get(roleName);
            if (role == null) {
                continue;
            }

            roleSet.add(role);
        }

        if (roleSet.isEmpty()) {
            return defaultRole == null ? new CopyOnWriteArraySet<>() : new CopyOnWriteArraySet<>(List.of(defaultRole));
        }

        return new CopyOnWriteArraySet<>(roleSet);
    }
}
