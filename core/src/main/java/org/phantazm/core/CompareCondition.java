package org.phantazm.core;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Methods of comparison between two numbers.
 */
@SuppressWarnings("DuplicatedCode")
public enum CompareCondition {
    LESS_THAN,
    LESS_THAN_OR_EQUAL_TO,
    EQUAL_TO,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL_TO;

    public boolean compare(double first, double second) {
        return switch (this) {
            case LESS_THAN -> first < second;
            case LESS_THAN_OR_EQUAL_TO -> first <= second;
            case EQUAL_TO -> first == second;
            case GREATER_THAN -> first > second;
            case GREATER_THAN_OR_EQUAL_TO -> first >= second;
        };
    }

    public boolean compare(float first, float second) {
        return switch (this) {
            case LESS_THAN -> first < second;
            case LESS_THAN_OR_EQUAL_TO -> first <= second;
            case EQUAL_TO -> first == second;
            case GREATER_THAN -> first > second;
            case GREATER_THAN_OR_EQUAL_TO -> first >= second;
        };
    }

    public boolean compare(long first, long second) {
        return switch (this) {
            case LESS_THAN -> first < second;
            case LESS_THAN_OR_EQUAL_TO -> first <= second;
            case EQUAL_TO -> first == second;
            case GREATER_THAN -> first > second;
            case GREATER_THAN_OR_EQUAL_TO -> first >= second;
        };
    }

    public boolean compare(int first, int second) {
        return switch (this) {
            case LESS_THAN -> first < second;
            case LESS_THAN_OR_EQUAL_TO -> first <= second;
            case EQUAL_TO -> first == second;
            case GREATER_THAN -> first > second;
            case GREATER_THAN_OR_EQUAL_TO -> first >= second;
        };
    }

    public boolean compare(short first, short second) {
        return switch (this) {
            case LESS_THAN -> first < second;
            case LESS_THAN_OR_EQUAL_TO -> first <= second;
            case EQUAL_TO -> first == second;
            case GREATER_THAN -> first > second;
            case GREATER_THAN_OR_EQUAL_TO -> first >= second;
        };
    }

    public boolean compare(byte first, byte second) {
        return switch (this) {
            case LESS_THAN -> first < second;
            case LESS_THAN_OR_EQUAL_TO -> first <= second;
            case EQUAL_TO -> first == second;
            case GREATER_THAN -> first > second;
            case GREATER_THAN_OR_EQUAL_TO -> first >= second;
        };
    }

    public <T extends Comparable<T>> boolean compareObject(@NotNull T first, @NotNull T second) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);

        int result = first.compareTo(second);

        return switch (this) {
            case LESS_THAN -> result < 0;
            case LESS_THAN_OR_EQUAL_TO -> result <= 0;
            case EQUAL_TO -> result == 0;
            case GREATER_THAN -> result > 0;
            case GREATER_THAN_OR_EQUAL_TO -> result >= 0;
        };
    }
}