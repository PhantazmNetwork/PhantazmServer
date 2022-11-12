package com.github.phantazmnetwork.zombies.powerup;

import com.github.phantazmnetwork.commons.Tickable;

public interface PowerupVisual extends Tickable {
    void spawn(double x, double y, double z);

    void despawn();
}
