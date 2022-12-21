package org.phantazm.zombies.powerup;

import org.phantazm.commons.Tickable;

public interface PowerupVisual extends Tickable {
    void spawn(double x, double y, double z);

    void despawn();
}
