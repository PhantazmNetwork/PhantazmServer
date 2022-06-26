package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

/**
 * Standard implementation of {@link Region3I}. It is expected that only trusted code will construct instances of this
 * record directly: the code must verify that the origin and lengths vectors comply with the general contract of
 * Region3I.
 * @param getOrigin the origin vector
 * @param getLengths the lengths vector (all components are non-negative)
 * @see Region3I
 */
record BasicRegion3I(@NotNull Vec3I getOrigin, @NotNull Vec3I getLengths) implements Region3I {
    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        if(obj instanceof Region3I other) {
            return getOrigin.equals(other.getOrigin()) && getLengths.equals(other.getLengths());
        }

        return false;
    }
}
