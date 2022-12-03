package com.github.phantazmnetwork.neuron.world;

import com.github.steanky.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

/**
 * Responsible for performing collision checks. Bounding boxes are represented by an origin (minimum) vector along with
 * a width, height, and depth (x-length, y-length, and z-length, respectively).
 */
public interface Collider {
    /**
     * Locates the highest solid that the given bounding box would collide with, if it were to translate itself by the
     * given vector (dX, dY, dZ).
     *
     * @param x      the x-component of the bounding box origin
     * @param y      the y-component of the bounding box origin
     * @param z      the z-component of the bounding box origin
     * @param width  the width of the bounding box (x-length)
     * @param height the height of the bounding box (y-length)
     * @param depth  the depths of the bounding box (z-length)
     * @param dX     the x-component of the translation vector
     * @param dY     the y-component of the translation vector
     * @param dZ     the z-component of the translation vector
     * @return the highest point, in world coordinates, of the highest solid colliding with the bounds as it moves along
     * the translation vector
     */
    double highestCollisionAlong(double x, double y, double z, double width, double height, double depth, double dX,
            double dY, double dZ);

    /**
     * Locates the lowest solid that the given bounding box would collide with, if it were to translate itself by the
     * given vector (dX, dY, dZ).
     *
     * @param x      the x-component of the bounding box origin
     * @param y      the y-component of the bounding box origin
     * @param z      the z-component of the bounding box origin
     * @param width  the width of the bounding box (x-length)
     * @param height the height of the bounding box (y-length)
     * @param depth  the depths of the bounding box (z-length)
     * @param dX     the x-component of the translation vector
     * @param dY     the y-component of the translation vector
     * @param dZ     the z-component of the translation vector
     * @return the lowest point, in world coordinates, of the lowest solid colliding with the bounds as it moves along
     * the translation vector
     */
    double lowestCollisionAlong(double x, double y, double z, double width, double height, double depth, double dX,
            double dY, double dZ);

    /**
     * Returns the y-coordinate of the highest face out of all collisions present in an origin-vector bounding box (x,
     * y, z, 1, 1, 1).
     *
     * @param x the x-component
     * @param y the y-component
     * @param z the z-component
     * @return the y-coordinate of the highest face, or y if no collisions exists here
     */
    double heightAt(int x, int y, int z);

    /**
     * Convenience overload for {@link Collider#heightAt(int, int, int)} that takes a {@link Vec3I}.
     *
     * @param vec3I the location to check for solids at
     * @return the height of the highest solid, if present, else the y-component of the given vector
     */
    default double heightAt(@NotNull Vec3I vec3I) {
        return heightAt(vec3I.x(), vec3I.y(), vec3I.z());
    }
}
