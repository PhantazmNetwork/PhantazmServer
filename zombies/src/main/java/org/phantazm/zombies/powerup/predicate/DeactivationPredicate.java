package org.phantazm.zombies.powerup.predicate;


public interface DeactivationPredicate {
    void activate(long time);

    boolean shouldDeactivate(long time);
}
