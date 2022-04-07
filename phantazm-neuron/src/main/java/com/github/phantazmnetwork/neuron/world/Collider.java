package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Responsible for performing basic collision checks. Most methods operate on axis-aligned bounding boxes (two double
 * triplets representing a rectangular region in 3D space).</p>
 *
 * <p>Those familiar with Bukkit and its implementation of bounding boxes should note that this class expects a slightly
 * different format. While Bukkit bounding boxes are represented by two vectors (one for each opposing corner of the
 * AABB), this class uses what is henceforth referred to as the <i>origin-vector</i> form. In origin-vector form, the
 * first vector (the "origin") represents one corner of the bounding box, and the second vector represents an offset —
 * a vector added to the origin to obtain the opposite corner from the first corner. Therefore, the lengths of each edge
 * of the AABB are represented by the absolute value of the corresponding components of the second vector.</p>
 *
 * <p>Collider implementations should not attempt to account for floating point imprecision issues. When performing
 * collision checks, the caller must shrink the bounding box by a tolerance value. The exact value that must be used
 * depends on the level of precision required as well as the numbers involved.</p>
 */
public interface Collider {
    double highestCollisionAlong(double x, double y, double z, double width, double height, double depth, double dX,
                                 double dY, double dZ);

    double lowestCollisionAlong(double x, double y, double z, double width, double height, double depth, double dX,
                                double dY, double dZ);

    /**
     * Returns the y-coordinate of the highest face out of all collisions present in an origin-vector bounding box (x,
     * y, z, 1, 1, 1).
     * @param x the x-component
     * @param y the y-component
     * @param z the z-component
     * @return the y-coordinate of the highest face, or y if no collisions exists here
     */
    double heightAt(int x, int y, int z);

    /**
     * Convenience overload for {@link Collider#heightAt(int, int, int)} that takes a {@link Vec3I}.
     * @param vec3I the location to check for solids at
     * @return the height of the highest solid, if present, else the y-component of the given vector
     */
    default double heightAt(@NotNull Vec3I vec3I) {
        return heightAt(vec3I.getX(), vec3I.getY(), vec3I.getZ());
    }
}
