package org.phantazm.stats.zombies;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ZombiesDatabase {
    private static final Object LOCK = new Object();

    private static LeaderboardDatabase database;

    private static <T> T requireDefined(T input) {
        if (input == null) {
            throw new IllegalStateException("Database has not been defined!");
        }

        return input;
    }

    public static void init(@NotNull LeaderboardDatabase database) {
        Objects.requireNonNull(database);
        synchronized (LOCK) {
            if (ZombiesDatabase.database != null) {
                throw new IllegalStateException("Database has already been defined");
            }

            ZombiesDatabase.database = database;
        }
    }

    public static @NotNull LeaderboardDatabase leaderboards() {
        return requireDefined(database);
    }
}
