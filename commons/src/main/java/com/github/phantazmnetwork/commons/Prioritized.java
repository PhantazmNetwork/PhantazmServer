package com.github.phantazmnetwork.commons;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an object that has a "priority" and may be ordered relative to other Prioritized instances. Prioritized
 * objects implement {@link Comparable} and are compared based on priority, from least to greatest. However, Prioritized
 * objects will generally <i>not</i> obey the general recommendation of {@link Comparable#compareTo(Object)} being
 * consistent with {@link Object#equals(Object)}.
 */
public interface Prioritized extends Comparable<Prioritized> {
    /**
     * Gets the priority of this object. This value should not change over the lifetime of the object.
     *
     * @return the priority of this object
     */
    int priority();

    @Override
    default int compareTo(@NotNull Prioritized o) {
        return Integer.compare(priority(), o.priority());
    }
}
