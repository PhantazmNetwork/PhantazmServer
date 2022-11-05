package com.github.phantazmnetwork.zombies.game.powerup;

import com.github.phantazmnetwork.commons.Tickable;

public interface PowerupVisual extends Tickable {
    void activate(double x, double y, double z);

    void despawn();
}
