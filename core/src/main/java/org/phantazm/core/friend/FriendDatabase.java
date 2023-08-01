package org.phantazm.core.friend;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface FriendDatabase {

    @NotNull CompletableFuture<List<Friend>> getFriends(@NotNull UUID playerUUID);

    @NotNull CompletableFuture<Boolean> hasFriend(@NotNull UUID playerUUID, @NotNull UUID friendUUID);

    @NotNull CompletableFuture<Void> addFriend(@NotNull UUID uuidA, @NotNull UUID uuidB);

    @NotNull CompletableFuture<Void> removeFriend(@NotNull UUID uuidA, @NotNull UUID uuidB);

}
