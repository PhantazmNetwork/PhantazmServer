package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

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
