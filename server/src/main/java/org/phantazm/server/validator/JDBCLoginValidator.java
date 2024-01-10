package org.phantazm.server.validator;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.FutureUtils;
import org.phantazm.stats.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JDBCLoginValidator implements LoginValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCLoginValidator.class);
    private static final CompletableFuture<LoginEntry> UNBANNED_FUTURE = FutureUtils.completedFuture(UNBANNED);
    private static final CompletableFuture<BanHistory> NEVER_BANNED_FUTURE = FutureUtils.completedFuture(NEVER_BANNED);

    private final Cache<UUID, LoginEntry> banCache;
    private final Cache<UUID, Boolean> whitelistCache;
    private final Cache<UUID, BanHistory> banHistoryCache;
    private final DataSource dataSource;
    private final Executor executor;

    public JDBCLoginValidator(@NotNull DataSource dataSource, @NotNull Executor executor) {
        this.banCache = Caffeine.newBuilder().maximumSize(1024).expireAfterAccess(Duration.ofMinutes(5)).build();
        this.whitelistCache = Caffeine.newBuilder().maximumSize(1024).expireAfterAccess(Duration.ofMinutes(5)).build();
        this.banHistoryCache = Caffeine.newBuilder().maximumSize(1024).expireAfterAccess(Duration.ofMinutes(5)).build();
        this.dataSource = Objects.requireNonNull(dataSource);
        this.executor = Objects.requireNonNull(executor);
    }

    private LoginEntry entryFor(UUID uuid) {
        return banCache.get(uuid, key -> {
            return DatabaseUtils.runPreparedSql(LOGGER, "entryFor", () -> UNBANNED, dataSource, """
                SELECT ban_reason, ban_date, unban_date FROM player_bans
                WHERE player_uuid=?
                LIMIT 1
                """, (connection, statement) -> {
                statement.setString(1, uuid.toString());
                statement.setFetchSize(1);

                ResultSet result = statement.executeQuery();
                if (!result.next()) {
                    return UNBANNED;
                }

                String banReason = result.getString(1);
                long banDate = result.getLong(2);
                Long unbanDate = result.getObject(3, Long.class);

                return new LoginEntry(false, banReason == null ? Component.empty() : MiniMessage.miniMessage()
                    .deserialize(banReason), banDate, unbanDate == null ? -1L : unbanDate);
            });
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> initTables() {
        return CompletableFuture.runAsync(() -> {
            DatabaseUtils.runSql(LOGGER, "initTables", dataSource, (connection, statement) -> {
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_bans (
                        player_uuid UUID NOT NULL PRIMARY KEY,
                        ban_reason TEXT,
                        ban_date BIGINT NOT NULL,
                        unban_date BIGINT
                    );
                    """);

                statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_ban_history (
                        player_uuid UUID NOT NULL PRIMARY KEY,
                        last_ban_date BIGINT NOT NULL,
                        ban_count INT NOT NULL
                    )
                    """);

                statement.execute("""
                    CREATE TABLE IF NOT EXISTS player_whitelist (
                        player_uuid UUID PRIMARY KEY NOT NULL
                    )
                    """);
            });
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<LoginEntry> login(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);

        LoginEntry entry = banCache.getIfPresent(uuid);
        if (entry != null) {
            if (entry.shouldUnban()) {
                pardon0(uuid);
                return UNBANNED_FUTURE;
            }

            return entry.equals(UNBANNED) ? UNBANNED_FUTURE : CompletableFuture.completedFuture(entry);
        }

        return CompletableFuture.supplyAsync(() -> entryFor(uuid), executor);
    }

    @Override
    public @NotNull CompletableFuture<BanHistory> history(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);

        BanHistory history = banHistoryCache.getIfPresent(uuid);
        if (history != null) {
            return history.equals(NEVER_BANNED) ? NEVER_BANNED_FUTURE : CompletableFuture.completedFuture(history);
        }

        return CompletableFuture.supplyAsync(() -> {
            return banHistoryCache.get(uuid, key -> {
                return DatabaseUtils.runPreparedSql(LOGGER, "history", dataSource, """
                    SELECT last_ban_date, ban_count FROM player_ban_history
                    WHERE player_uuid=?
                    LIMIT 1
                    """, (connection, statement) -> {
                    statement.setString(1, uuid.toString());
                    statement.setFetchSize(1);

                    ResultSet result = statement.executeQuery();
                    if (!result.next()) {
                        return NEVER_BANNED;
                    }

                    return new BanHistory(result.getLong(1), result.getInt(2));
                });
            });
        }, executor);
    }

    @Override
    public void clearHistory(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);

        executor.execute(() -> {
            DatabaseUtils.runPreparedSql(LOGGER, "clearHistory", dataSource, """
                DELETE FROM player_ban_history
                WHERE player_uuid=?
                """, (connection, statement) -> {
                statement.setString(1, uuid.toString());
                statement.execute();
            });

            banHistoryCache.invalidate(uuid);
        });
    }

    @Override
    public void ban(@NotNull UUID uuid, @NotNull Component reason, long unbanDate) {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(reason);

        long banDate = Instant.now().getEpochSecond();
        executor.execute(() -> {
            String banReason = MiniMessage.miniMessage().serialize(reason);

            DatabaseUtils.runPreparedSql(LOGGER, "ban", dataSource, """
                INSERT INTO player_bans (player_uuid, ban_reason, ban_date, unban_date)
                VALUES(?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    ban_reason=?,
                    ban_date=?,
                    unban_date=?
                """, (connection, statement) -> {
                String banReasonOrNull = banReason.isEmpty() ? null : banReason;
                String uuidString = uuid.toString();

                statement.setString(1, uuidString);
                statement.setString(2, banReasonOrNull);

                statement.setLong(3, banDate);
                statement.setLong(4, unbanDate);
                statement.setString(5, banReasonOrNull);

                statement.setLong(6, banDate);
                statement.setLong(7, unbanDate);

                statement.execute();

                PreparedStatement secondStatement = connection.prepareStatement("""
                    INSERT INTO player_ban_history (player_uuid, last_ban_date, ban_count)
                    VALUES(?, ?, 1)
                    ON DUPLICATE KEY UPDATE
                        last_ban_date=?,
                        ban_count=ban_count+1
                    """);
                secondStatement.setString(1, uuidString);
                secondStatement.setLong(2, banDate);
                secondStatement.setLong(3, banDate);

                secondStatement.execute();
            });

            banCache.put(uuid, new LoginEntry(false, banReason.isEmpty() ? null : reason, banDate, unbanDate));
        });
    }

    @Override
    public boolean isBanned(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);

        LoginEntry entry = entryFor(uuid);
        if (entry.shouldUnban()) {
            pardon0(uuid);
            return false;
        }

        return !entry.canJoin();
    }

    private void pardon0(UUID uuid) {
        executor.execute(() -> {
            DatabaseUtils.runPreparedSql(LOGGER, "pardon0", dataSource, """
                DELETE FROM player_bans
                WHERE player_uuid=?
                """, (connection, statement) -> {
                statement.setString(1, uuid.toString());
                statement.execute();
            });

            banCache.invalidate(uuid);
        });
    }

    @Override
    public void pardon(@NotNull UUID uuid) {
        pardon0(uuid);
    }

    @Override
    public void addWhitelist(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);

        executor.execute(() -> {
            DatabaseUtils.runPreparedSql(LOGGER, "addWhitelist", dataSource, """
                INSERT INTO player_whitelist (player_uuid)
                VALUES(?)
                ON DUPLICATE KEY UPDATE
                    player_uuid=?
                """, (connection, statement) -> {
                String uuidString = uuid.toString();
                statement.setString(1, uuidString);
                statement.setString(2, uuidString);
                statement.execute();
            });

            whitelistCache.put(uuid, true);
        });
    }

    @Override
    public boolean isWhitelisted(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);

        return whitelistCache.get(uuid, key -> {
            return DatabaseUtils.runPreparedSql(LOGGER, "isWhitelisted", () -> true, dataSource, """
                SELECT player_uuid FROM player_whitelist
                WHERE player_uuid=?
                LIMIT 1
                """, (connection, statement) -> {
                statement.setString(1, uuid.toString());
                statement.setFetchSize(1);

                ResultSet result = statement.executeQuery();
                return result.next();
            });
        });
    }

    @Override
    public void removeWhitelist(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);

        executor.execute(() -> {
            DatabaseUtils.runPreparedSql(LOGGER, "addWhitelist", dataSource, """
                DELETE FROM player_whitelist
                WHERE player_uuid=?
                """, (connection, statement) -> {
                statement.setString(1, uuid.toString());
                statement.execute();
            });

            whitelistCache.invalidate(uuid);
        });
    }
}
