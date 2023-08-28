package org.phantazm.server.validator;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.steanky.toolkit.function.ThrowingRunnable;
import com.github.steanky.toolkit.function.ThrowingSupplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static org.jooq.impl.DSL.*;

public class DatabaseLoginValidator implements LoginValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseLoginValidator.class);

    private final Cache<UUID, LoginEntry> banCache;
    private final Cache<UUID, Boolean> whitelistCache;
    private final Cache<UUID, BanHistory> banHistoryCache;
    private final DataSource dataSource;
    private final Executor executor;

    public DatabaseLoginValidator(@NotNull DataSource dataSource, @NotNull Executor executor) {
        this.banCache = Caffeine.newBuilder().maximumSize(1024).expireAfterAccess(Duration.ofMinutes(5)).build();
        this.whitelistCache = Caffeine.newBuilder().maximumSize(1024).expireAfterAccess(Duration.ofMinutes(5)).build();
        this.banHistoryCache = Caffeine.newBuilder().maximumSize(1024).expireAfterAccess(Duration.ofMinutes(5)).build();
        this.dataSource = Objects.requireNonNull(dataSource);
        this.executor = Objects.requireNonNull(executor);
    }

    private static void write(ThrowingRunnable<SQLException> supplier) {
        try {
            supplier.run();
        } catch (SQLException e) {
            LOGGER.warn("Exception when writing to ban database", e);
        }
    }

    private static <T> T read(ThrowingSupplier<? extends Record, ? extends SQLException> reader,
        Function<? super Record, ? extends T> mapper, T defaultValue) {
        try {
            return mapper.apply(reader.get());
        } catch (SQLException e) {
            LOGGER.warn("Exception when querying ban database", e);
        }

        return defaultValue;
    }

    private static LoginEntry loginEntryFromRecord(Record result) {
        if (result == null) {
            return UNBANNED;
        }

        String banReason = result.get("ban_reason", String.class);
        long banDate = result.get("ban_date", long.class);
        Long unbanDate = result.get("unban_date", Long.class);

        return new LoginEntry(false, banReason == null ? Component.empty() : MiniMessage.miniMessage()
            .deserialize(banReason), banDate, unbanDate == null ? -1L : unbanDate);
    }

    private static BanHistory banHistoryFromRecord(Record result) {
        if (result == null) {
            return NEVER_BANNED;
        }

        long lastBanDate = result.get("last_ban_date", long.class);
        int banCount = result.get("ban_count", int.class);

        return new BanHistory(lastBanDate, banCount);
    }

    private LoginEntry entryFor(UUID uuid) {
        return banCache.get(uuid, key -> {
            return read(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    return using(connection).selectFrom(table("player_bans")).where(field("player_uuid").eq(key))
                        .fetchOne();
                }
            }, DatabaseLoginValidator::loginEntryFromRecord, UNBANNED);
        });
    }

    @Override
    public @NotNull CompletableFuture<LoginEntry> login(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);

        LoginEntry entry = banCache.getIfPresent(uuid);
        if (entry != null) {
            if (entry.shouldUnban()) {
                pardon0(uuid);
                return CompletableFuture.completedFuture(UNBANNED);
            }

            return CompletableFuture.completedFuture(entry);
        }

        return CompletableFuture.supplyAsync(() -> entryFor(uuid), executor);
    }

    @Override
    public @NotNull CompletableFuture<BanHistory> history(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);

        BanHistory history = banHistoryCache.getIfPresent(uuid);
        if (history != null) {
            return CompletableFuture.completedFuture(history);
        }

        return CompletableFuture.supplyAsync(() -> {
            return banHistoryCache.get(uuid, key -> {
                return read(() -> {
                    try (Connection connection = dataSource.getConnection()) {
                        return using(connection).selectFrom(table("player_ban_history"))
                            .where(field("player_uuid").eq(key))
                            .fetchOne();
                    }
                }, DatabaseLoginValidator::banHistoryFromRecord, NEVER_BANNED);
            });
        }, executor);
    }

    @Override
    public void clearHistory(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);

        executor.execute(() -> {
            write(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    using(connection).deleteFrom(table("player_ban_history"))
                        .where(field("player_uuid").eq(uuid)).execute();
                }
            });

            banHistoryCache.invalidate(uuid);
        });
    }

    @Override
    public void ban(@NotNull UUID uuid, @NotNull Component reason, long unbanDate) {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(reason);

        long banDate = System.currentTimeMillis() / 1000L;
        banCache.put(uuid, new LoginEntry(false, reason, banDate, unbanDate));

        executor.execute(() -> {
            String banReason = MiniMessage.miniMessage().serialize(reason);
            if (banReason.isEmpty()) {
                banReason = null;
            }

            String finalBanReason = banReason;
            write(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    using(connection).insertInto(table("player_bans"),
                            field("player_uuid"),
                            field("ban_reason"),
                            field("ban_date"),
                            field("unban_date"))
                        .values(uuid, finalBanReason, banDate, unbanDate < 0 ? null : unbanDate).onDuplicateKeyUpdate()
                        .set(field("ban_reason"), finalBanReason)
                        .set(field("ban_date"), banDate)
                        .set(field("unban_date"), unbanDate < 0 ? null : unbanDate)
                        .execute();
                }

                try (Connection connection = dataSource.getConnection()) {
                    using(connection).insertInto(table("player_ban_history"),
                            field("player_uuid"),
                            field("last_ban_date"),
                            field("ban_count"))
                        .values(uuid, banDate, 1)
                        .onDuplicateKeyUpdate()
                        .set(field("last_ban_date"), banDate)
                        .set(field("ban_count"), (Field<?>) field("ban_count").plus(1))
                        .execute();
                }
            });
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
            write(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    using(connection).deleteFrom(table("player_bans")).where(field("player_uuid").eq(uuid)).execute();
                }
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

        whitelistCache.put(uuid, true);

        executor.execute(() -> {
            write(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    using(connection).insertInto(table("player_whitelist"), field("player_uuid")).values(uuid)
                        .onDuplicateKeyUpdate().set(field("player_uuid"), uuid).execute();
                }
            });
        });
    }

    @Override
    public boolean isWhitelisted(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);

        return whitelistCache.get(uuid, key -> {
            return read(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    return using(connection).selectFrom(table("player_whitelist")).where(field("player_uuid").eq(key))
                        .fetchOne();
                }
            }, Objects::nonNull, true);
        });
    }

    @Override
    public void removeWhitelist(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);

        executor.execute(() -> {
            write(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    using(connection).deleteFrom(table("player_whitelist")).where(field("player_uuid").eq(uuid))
                        .execute();
                }
            });

            whitelistCache.invalidate(uuid);
        });
    }
}
