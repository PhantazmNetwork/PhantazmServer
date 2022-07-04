package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

/**
 * Standard implementation of {@link Region3I}. It is expected that only trusted code will construct instances of this
 * record directly: the code must verify that the origin and lengths vectors comply with the general contract of
 * Region3I.
 */
record BasicRegion3I(Vec3I origin, Vec3I lengths) implements Region3I {
    /**
     * Creates a new instance of this record.
     * @param origin the origin vector
     * @param lengths the lengths vector
     */
    BasicRegion3I(@NotNull Vec3I origin, @NotNull Vec3I lengths) {
        this.origin = origin.immutable();
        this.lengths = lengths.immutable();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof Region3I other) {
            return origin.equals(other.origin()) && lengths.equals(other.lengths());
        }

        return false;
    }

    @Override
    public @NotNull Vec3I origin() {
        return origin;
    }

    @Override
    public @NotNull Vec3I lengths() {
        return lengths;
    }
}
