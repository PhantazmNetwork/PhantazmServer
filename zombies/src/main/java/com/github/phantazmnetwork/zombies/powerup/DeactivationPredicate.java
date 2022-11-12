package com.github.phantazmnetwork.zombies.powerup;


public interface DeactivationPredicate {
    void activate(long time);

    boolean shouldDeactivate(long time);
}
