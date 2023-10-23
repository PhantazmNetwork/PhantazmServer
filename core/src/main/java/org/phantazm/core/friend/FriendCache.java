package org.phantazm.core.friend;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class FriendCache {

    private final FriendDatabase database;

    private final Cache<UUID, List<Friend>> uuidToFriends;

    private final Map<UUID, Object> locks = new ConcurrentHashMap<>();

    private final Executor executor;

    public FriendCache(@NotNull FriendDatabase database, @NotNull Duration expiration, @NotNull Executor executor) {
        this.database = Objects.requireNonNull(database);
        this.uuidToFriends = Caffeine
            .newBuilder()
            .expireAfterAccess(expiration)
            .build();
        this.executor = Objects.requireNonNull(executor);
    }

    public CompletableFuture<List<Friend>> getFriends(@NotNull UUID playerUUID) {
        List<Friend> friends = uuidToFriends.getIfPresent(playerUUID);
        if (friends != null) {
            return CompletableFuture.completedFuture(friends);
        }

        return CompletableFuture.supplyAsync(() -> {
            Object lock = locks.computeIfAbsent(playerUUID, unused -> new Object());

            synchronized (lock) {
                List<Friend> newFriends = uuidToFriends.getIfPresent(playerUUID);
                if (newFriends != null) {
                    return newFriends;
                }

                List<Friend> databaseFriends = database.getFriends(playerUUID).join();
                uuidToFriends.put(playerUUID, databaseFriends);
                return databaseFriends;
            }
        }, executor);
    }

}
