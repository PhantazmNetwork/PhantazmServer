package org.phantazm.core.friend;

import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.jooq.impl.DSL.*;

public class SQLFriendDatabase implements FriendDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLFriendDatabase.class);

    private final DataSource dataSource;

    private final Executor executor;

    public SQLFriendDatabase(@NotNull DataSource dataSource, @NotNull Executor executor) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    @Override
    public @NotNull CompletableFuture<List<Friend>> getFriends(@NotNull UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                Result<Record1<UUID>> result =
                        using(connection).select(field("friend_uuid", SQLDataType.UUID)).from("friend")
                                .where(field("player_uuid").eq(playerUUID)).fetch();

                List<Friend> friends = new ArrayList<>(result.size());
                for (Record1<UUID> record : result) {
                    friends.add(new Friend(record.component1()));
                }

                return friends;
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor).whenComplete(this::logException);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> hasFriend(@NotNull UUID playerUUID, @NotNull UUID friendUUID) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                return using(connection).fetchExists(
                        select(field("friend_uuid")).from("friend").where(field("player_uuid").eq(playerUUID))
                                .and(field("friend_uuid").eq(friendUUID)));
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor).whenComplete(this::logException);
    }

    @Override
    public @NotNull CompletableFuture<Void> addFriend(@NotNull UUID uuidA, @NotNull UUID uuidB) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                using(connection).insertInto(table("friend"), field("player_uuid"), field("friend_uuid"))
                        .values(uuidA, uuidB).values(uuidB, uuidA).execute();
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor).whenComplete(this::logException);
    }

    @Override
    public @NotNull CompletableFuture<Void> removeFriend(@NotNull UUID uuidA, @NotNull UUID uuidB) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                DSLContext context = using(connection);
                context.deleteFrom(table("friend")).where(field("player_uuid").eq(uuidA))
                        .and(field("target_uuid").eq(uuidB))
                        .execute();
                context.deleteFrom(table("friend")).where(field("player_uuid").eq(uuidB))
                        .and(field("target_uuid").eq(uuidA))
                        .execute();
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor).whenComplete(this::logException);
    }

    private <T> void logException(T ignored, Throwable throwable) {
        if (throwable != null) {
            LOGGER.warn("Exception while querying database", throwable);
        }
    }

}
