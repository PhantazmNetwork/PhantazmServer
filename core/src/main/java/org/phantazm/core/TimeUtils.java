package org.phantazm.core;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class TimeUtils {
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SECONDS_PER_HOUR = 3600L;
    public static final long SECONDS_PER_DAY = 86400L;
    public static final long SECONDS_PER_MONTH = 2592000L;
    public static final long SECONDS_PER_YEAR = 31104000L;

    private TimeUtils() {
    }

    public static long stringToSimpleDuration(@NotNull String string) {
        Objects.requireNonNull(string);
        if (string.isEmpty()) {
            return -1L;
        }

        String substring = string.substring(0, string.length() - 1);
        long value = safeParse(substring);
        if (value < 0) {
            return value;
        }

        return switch (string.charAt(string.length() - 1)) {
            case 's' -> value;
            case 'm' -> value * SECONDS_PER_MINUTE;
            case 'h' -> value * SECONDS_PER_HOUR;
            case 'd' -> value * SECONDS_PER_DAY;
            case 'M' -> value * SECONDS_PER_MONTH;
            case 'y' -> value * SECONDS_PER_YEAR;
            default -> -1;
        };
    }

    private static long safeParse(String string) {
        try {
            return Long.parseLong(string);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}
