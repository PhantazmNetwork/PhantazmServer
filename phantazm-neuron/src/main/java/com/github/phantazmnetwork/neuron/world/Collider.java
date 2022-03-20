package com.github.phantazmnetwork.neuron.world;

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
 */
public interface Collider {
    /**
     * Returns a value representing the y-coordinate of the top face of the highest collision intersecting the path
     * travelled by a bounding box (in origin-vector form) along the movement vector (dX, dY, dZ). The bounding box must
     * be in normalized origin-vector form. Colliding objects overlapping the initial position of the bounding box will
     * not be considered.
     * @param oX x-component of the origin vector
     * @param oY y-component of the origin vector
     * @param oZ z-component of the origin vector
     * @param vX x-component of the offset vector
     * @param vY y-component of the offset vector
     * @param vZ z-component of the offset vector
     * @param dX x-component of the movement vector
     * @param dY y-component of the movement vector
     * @param dZ z-component of the movement vector
     * @return the y-coordinate of the top face of the highest colliding solid in world-space coordinates, or
     * Double.NEGATIVE_INFINITY if no collision exists
     */
    double highestCollisionAlong(double oX, double oY, double oZ, double vX, double vY, double vZ, double dX, double dY,
                                 double dZ);

    /**
     * Returns a value representing the y-coordinate of the bottom face of the lowest collision intersecting the path
     * travelled by a bounding box (in origin-vector form) along the movement vector (dX, dY, dZ). The bounding box must
     * be in normalized origin-vector form. Colliding objects overlapping the initial position of the bounding box will
     * not be considered.
     * @param oX x-component of the origin vector
     * @param oY y-component of the origin vector
     * @param oZ z-component of the origin vector
     * @param vX x-component of the offset vector
     * @param vY y-component of the offset vector
     * @param vZ z-component of the offset vector
     * @param dX x-component of the movement vector
     * @param dY y-component of the movement vector
     * @param dZ z-component of the movement vector
     * @return the y-coordinate of the bottom face of the highest colliding solid in world-space coordinates, or
     * Double.POSITIVE_INFINITY if no collision exists
     */
    double lowestCollisionAlong(double oX, double oY, double oZ, double vX, double vY, double vZ, double dX, double dY,
                                double dZ);

    /**
     * Returns the y-coordinate of the highest face out of all collisions present in a hypothetical origin-vector
     * bounding box (x, y, z, 1, 1, 1).
     * @param x the x-component
     * @param y the y-component
     * @param z the z-component
     * @return the y-coordinate of the highest face, or y if no collisions exists here
     */
    double heightAt(int x, int y, int z);
}
