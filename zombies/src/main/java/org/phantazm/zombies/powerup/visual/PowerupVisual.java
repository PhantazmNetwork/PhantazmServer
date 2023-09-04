package org.phantazm.zombies.powerup.visual;

import net.minestom.server.Tickable;

public interface PowerupVisual extends Tickable {
    void spawn(double x, double y, double z);

    void despawn();
}
