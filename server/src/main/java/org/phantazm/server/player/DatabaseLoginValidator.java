package org.phantazm.server.player;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.jooq.Record;

import static org.jooq.impl.DSL.*;

public class DatabaseLoginValidator implements LoginValidator {
    private static final BooleanObjectPair<Component> UNBANNED = BooleanObjectPair.of(true, null);

    private final Cache<UUID, BooleanObjectPair<Component>> banCache;
    private final Cache<UUID, Boolean> whitelistCache;
    private final Connection connection;
    private final Executor executor;

    public DatabaseLoginValidator(@NotNull Connection connection, @NotNull Executor executor) {
        this.banCache =
                Caffeine.newBuilder().softValues().maximumSize(2048).expireAfterAccess(Duration.ofMinutes(5)).build();
        this.whitelistCache =
                Caffeine.newBuilder().softValues().maximumSize(2048).expireAfterAccess(Duration.ofMinutes(5)).build();
        this.connection = Objects.requireNonNull(connection, "connection");
        this.executor = executor;
    }

    @Override
    public @NotNull BooleanObjectPair<Component> validateLogin(@NotNull UUID uuid) {
        return banCache.get(uuid, key -> {
            Record result =
                    using(connection).selectFrom(table("player_bans")).where(field("player_uuid").eq(key.toString()))
                            .fetchOne();
            return fromRecord(result);
        });
    }

    @Override
    public void ban(@NotNull UUID uuid, @NotNull Component reason) {
        banCache.put(uuid, BooleanObjectPair.of(false, reason));

        executor.execute(() -> {
            String banReason = MiniMessage.miniMessage().serialize(reason);
            using(connection).insertInto(table("player_bans"), field("player_uuid"), field("ban_reason"))
                    .values(uuid.toString(), banReason).onDuplicateKeyUpdate().set(field("ban_reason"), banReason)
                    .execute();
        });
    }

    @Override
    public boolean isBanned(@NotNull UUID uuid) {
        BooleanObjectPair<Component> banned = banCache.get(uuid, key -> {
            Record result =
                    using(connection).selectFrom(table("player_bans")).where(field("player_uuid").eq(key.toString()))
                            .fetchOne();
            return fromRecord(result);
        });

        return !banned.firstBoolean();
    }

    @Override
    public void pardon(@NotNull UUID uuid) {
        banCache.invalidate(uuid);

        executor.execute(() -> {
            using(connection).deleteFrom(table("player_bans")).where(field("player_uuid").eq(uuid)).execute();
        });
    }

    @Override
    public void addWhitelist(@NotNull UUID uuid) {
        whitelistCache.put(uuid, true);

        executor.execute(() -> {
            using(connection).insertInto(table("player_whitelist"), field("player_uuid")).onDuplicateKeyUpdate()
                    .set(field("player_uuid"), uuid.toString()).execute();
        });
    }

    @Override
    public boolean isWhitelisted(@NotNull UUID uuid) {
        return whitelistCache.get(uuid, key -> {
            Record record = using(connection).selectFrom(table("player_whitelist"))
                    .where(field("player_uuid").eq(key.toString())).fetchOne();
            return record != null;
        });
    }

    @Override
    public void removeWhitelist(@NotNull UUID uuid) {
        whitelistCache.invalidate(uuid);

        executor.execute(() -> {
            using(connection).deleteFrom(table("player_whitelist")).where(field("player_uuid").eq(uuid)).execute();
        });
    }

    @Override
    public void flush() {

    }

    private static BooleanObjectPair<Component> fromRecord(Record result) {
        if (result == null) {
            return UNBANNED;
        }

        String banReason = result.get("ban_reason", String.class);
        return BooleanObjectPair.of(false, MiniMessage.miniMessage().deserialize(banReason));
    }
}
