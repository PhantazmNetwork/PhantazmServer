package org.phantazm.server.validator;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface LoginValidator {
    record LoginEntry(boolean canJoin,
        @Nullable Component banReason,
        long banDate,
        long unbanDate) {
        public boolean shouldUnban() {
            return unbanDate >= 0 && System.currentTimeMillis() / 1000L > unbanDate;
        }
    }

    record BanHistory(long lastBanDate,
        int banCount) {
    }

    LoginEntry UNBANNED = new LoginEntry(true, null, -1L, -1L);

    BanHistory NEVER_BANNED = new BanHistory(-1, 0);

    Component NOT_WHITELISTED_MESSAGE = Component.text("You are not whitelisted on this server!");

    @NotNull CompletableFuture<LoginEntry> login(@NotNull UUID uuid);

    @NotNull CompletableFuture<BanHistory> history(@NotNull UUID uuid);

    void clearHistory(@NotNull UUID uuid);

    default void ban(@NotNull UUID uuid, @NotNull Component reason) {
        ban(uuid, reason, -1);
    }

    void ban(@NotNull UUID uuid, @NotNull Component reason, long unbanDate);

    boolean isBanned(@NotNull UUID uuid);

    void pardon(@NotNull UUID uuid);

    void addWhitelist(@NotNull UUID uuid);

    boolean isWhitelisted(@NotNull UUID uuid);

    void removeWhitelist(@NotNull UUID uuid);
}
