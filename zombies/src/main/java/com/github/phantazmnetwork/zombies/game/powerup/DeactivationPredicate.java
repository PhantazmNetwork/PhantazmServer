package com.github.phantazmnetwork.zombies.game.powerup;


public interface DeactivationPredicate {
    void activate(long time);

    boolean shouldDeactivate(long time);
}
