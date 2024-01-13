package org.phantazm.stats;

import org.jetbrains.annotations.NotNull;
import org.phantazm.stats.general.UsernameDatabase;

import java.util.*;

/**
 * Globally-relevant databases.
 */
public final class Databases {
    private static final Object LOCK = new Object();

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

    /**
     * Defines the {@link UsernameDatabase} used by this server. May only be called once, preferably during server
     * startup before players have had the chance to join.
     *
     * @param usernameDatabase the UsernameDatabase to be used by this server
     */
    public static void init(@NotNull UsernameDatabase usernameDatabase) {
        synchronized (LOCK) {
            requireUndefined(Databases.usernameDatabase);
            Databases.usernameDatabase = Objects.requireNonNull(usernameDatabase);
        }
    }

    /**
     * The database used for username caching.
     *
     * @return the {@link UsernameDatabase} used by this server instance
     */
    public static @NotNull UsernameDatabase usernames() {
        return requireDefined(usernameDatabase);
    }
}
