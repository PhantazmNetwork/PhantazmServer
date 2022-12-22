package org.phantazm.neuron.world;

import com.github.steanky.vector.Vec3D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

/**
 * <p>Represents an object that has collision. Each Solid is a single AABB (axis-aligned bounding box) specified by two
 * points (the "minimum" point and "maximum" point, with the region between encompassing the bounds). Platform-specific
 * bindings must generally create their own implementation of this interface.</p>
 *
 * <p>Solid implementations should be considered equal if and only if their min and max vectors are equal, and their
 * children sets are equal (as defined in {@link Set#equals(Object)}).</p>
 */
public interface Solid {
    /**
     * Reports if this solid has "child" components.
     *
     * @return {@code true} if this solid has child components, {@code false} otherwise
     */
    boolean hasChildren();

    /**
     * Retrieves the minimum floating-point vector for this solid. Each component must range from [0.0, 1.0] and be
     * smaller than or equal to each equivalent component in the vector returned by {@code getMax()}.
     *
     * @return the minimum vector
     */
    @NotNull Vec3D getMin();

    /**
     * Retrieves the maximum floating-point vector for this solid. Each component must range from [0.0, 1.0] and be
     * greater than or equal to each equivalent component in the vector returned by {@code getMin()}.
     *
     * @return the minimum vector
     */
    @NotNull Vec3D getMax();

    /**
     * Returns an {@link Iterable} over any child solids. Will be empty if this solid does not have any children.
     *
     * @return an iterable over any child solids
     */
    @NotNull @Unmodifiable Set<Solid> getChildren();

    /**
     * Determines if this solid  overlaps the given bounds. The solid itself is considered relative to the origin
     * (0, 0, 0).
     *
     * @param x      the x-component of the minimum vector
     * @param y      the y-component of the minimum vector
     * @param z      the z-component of the minimum vector
     * @param width  the width of the bounding box (x-length)
     * @param height the height of the bounding box (y-length)
     * @param depth  the depth of the bounding box (z-length)
     * @return {@code true} if the given bounding box overlaps this solid, false otherwise
     */
    boolean overlaps(double x, double y, double z, double width, double height, double depth);
}
