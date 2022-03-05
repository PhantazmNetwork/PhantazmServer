package com.github.phantazmnetwork.neuron.world;

public interface Collider {
    boolean collidesAt(int x, int y, int z);

    boolean collidesMovingAlong(int fromX, int fromY, int fromZ, int moveX, int moveY, int moveZ);

    float findLowest(int x, int y, int z, float limit);

    float findHighest(int x, int y, int z, float limit);
}
