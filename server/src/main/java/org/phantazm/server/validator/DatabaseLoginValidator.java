package org.phantazm.server.validator;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.steanky.toolkit.function.ThrowingRunnable;
import com.github.steanky.toolkit.function.ThrowingSupplier;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Function;

import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import static org.jooq.impl.DSL.*;

public class DatabaseLoginValidator implements LoginValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseLoginValidator.class);
    private static final BooleanObjectPair<Component> UNBANNED = BooleanObjectPair.of(true, null);

    private final Cache<UUID, BooleanObjectPair<Component>> banCache;
    private final Cache<UUID, Boolean> whitelistCache;
    private final DataSource dataSource;
    private final Executor executor;

    public DatabaseLoginValidator(@NotNull DataSource dataSource, @NotNull Executor executor) {
        this.banCache = Caffeine.newBuilder().maximumSize(1024).expireAfterAccess(Duration.ofMinutes(5)).build();
        this.whitelistCache = Caffeine.newBuilder().maximumSize(1024).expireAfterAccess(Duration.ofMinutes(5)).build();
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public @NotNull BooleanObjectPair<Component> validateLogin(@NotNull UUID uuid) {
        return banCache.get(uuid, key -> {
            return read(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    return using(connection).selectFrom(table("player_bans")).where(field("player_uuid").eq(key))
                            .fetchOne();
                }
            }, DatabaseLoginValidator::fromRecord, UNBANNED);
        });
    }

    @Override
    public void ban(@NotNull UUID uuid, @NotNull Component reason) {
        banCache.put(uuid, BooleanObjectPair.of(false, reason));

        executor.execute(() -> {
            String banReason = MiniMessage.miniMessage().serialize(reason);
            if (banReason.isEmpty()) {
                banReason = null;
            }

            String finalBanReason = banReason;
            write(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    using(connection).insertInto(table("player_bans"), field("player_uuid"), field("ban_reason"))
                            .values(uuid, finalBanReason).onDuplicateKeyUpdate()
                            .set(field("ban_reason"), finalBanReason).execute();
                }
            });
        });
    }

    @Override
    public boolean isBanned(@NotNull UUID uuid) {
        BooleanObjectPair<Component> banned = banCache.get(uuid, key -> {
            return read(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    return using(connection).selectFrom(table("player_bans")).where(field("player_uuid").eq(key))
                            .fetchOne();
                }
            }, DatabaseLoginValidator::fromRecord, UNBANNED);
        });

        return !banned.firstBoolean();
    }

    @Override
    public void pardon(@NotNull UUID uuid) {
        banCache.invalidate(uuid);

        executor.execute(() -> {
            write(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    using(connection).deleteFrom(table("player_bans")).where(field("player_uuid").eq(uuid)).execute();
                }
            });
        });
    }

    @Override
    public void addWhitelist(@NotNull UUID uuid) {
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
        whitelistCache.invalidate(uuid);

        executor.execute(() -> {
            write(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    using(connection).deleteFrom(table("player_whitelist")).where(field("player_uuid").eq(uuid))
                            .execute();
                }
            });
        });
    }

    private static void write(ThrowingRunnable<SQLException> supplier) {
        try {
            supplier.run();
        }
        catch (SQLException e) {
            LOGGER.warn("Exception when writing to ban database", e);
        }
    }

    private static <T> T read(ThrowingSupplier<? extends Record, ? extends SQLException> reader,
            Function<? super Record, ? extends T> mapper, T defaultValue) {
        try {
            return mapper.apply(reader.get());
        }
        catch (SQLException e) {
            LOGGER.warn("Exception when querying ban database", e);
        }

        return defaultValue;
    }

    private static BooleanObjectPair<Component> fromRecord(Record result) {
        if (result == null) {
            return UNBANNED;
        }

        String banReason = result.get("ban_reason", String.class);
        return BooleanObjectPair.of(false,
                banReason == null ? Component.empty() : MiniMessage.miniMessage().deserialize(banReason));
    }
}
