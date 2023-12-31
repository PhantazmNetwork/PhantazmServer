package org.phantazm.stats;

import org.jetbrains.annotations.NotNull;
import org.phantazm.stats.general.UsernameDatabase;
import org.phantazm.stats.zombies.LeaderboardDatabase;

import java.util.*;

public final class Databases {
    private static final Object LOCK = new Object();

    private static LeaderboardDatabase leaderboardDatabase;
    private static UsernameDatabase usernameDatabase;

    private static <T> T requireDefined(T input) {
        if (input == null) {
            throw new IllegalStateException("Database has not been defined!");
        }

        return input;
    }

    private static void requireUndefined(Object input) {
        if (input != null) {
            throw new IllegalStateException("Database has already been defined");
        }
    }

    public static void init(@NotNull LeaderboardDatabase leaderboardDatabase) {
        Objects.requireNonNull(leaderboardDatabase);
        synchronized (LOCK) {
            requireUndefined(Databases.leaderboardDatabase);
            Databases.leaderboardDatabase = Objects.requireNonNull(leaderboardDatabase);
        }
    }

    public static void init(@NotNull UsernameDatabase usernameDatabase) {
        synchronized (LOCK) {
            requireUndefined(Databases.usernameDatabase);
            Databases.usernameDatabase = Objects.requireNonNull(usernameDatabase);
        }
    }

    public static @NotNull LeaderboardDatabase leaderboards() {
        return requireDefined(leaderboardDatabase);
    }

    public static @NotNull UsernameDatabase usernames() {
        return requireDefined(usernameDatabase);
    }
}
