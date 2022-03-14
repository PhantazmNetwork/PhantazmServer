package com.github.phantazmnetwork.neuron.world;

public interface Collider {
    double highestCollisionAlong(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                                 double deltaX, double deltaY, double deltaZ);

    double smallestCollisionAlong(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                                  double deltaX, double deltaY, double deltaZ);

    double highestCollisionAt(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

    double heightAt(int x, int y, int z);
}
