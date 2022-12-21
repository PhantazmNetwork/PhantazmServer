package org.phantazm.zombies.powerup;


public interface DeactivationPredicate {
    void activate(long time);

    boolean shouldDeactivate(long time);
}
