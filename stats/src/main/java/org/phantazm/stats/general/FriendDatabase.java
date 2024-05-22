package org.phantazm.stats.general;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface FriendDatabase {
    record FriendEntry(UUID player, long time) {}

    @NotNull CompletableFuture<Void> initTables();

    @NotNull CompletableFuture<Set<UUID>> fetchNFriends(@NotNull UUID requester, int numberOfRows, int offset);

    // unused - use only for stat API
    @NotNull CompletableFuture<Set<FriendEntry>> fetchNFriendsWithTime(@NotNull UUID requester, int numberOfRows, int offset);

    @NotNull CompletableFuture<Boolean> validateFriend(@NotNull UUID player1, @NotNull UUID player2);

    @NotNull CompletableFuture<Void> submitFriend(@NotNull UUID player1, @NotNull UUID player2, long timeCreated);

    @NotNull CompletableFuture<Void> deleteFriend(@NotNull UUID player1, @NotNull UUID player2);
}
