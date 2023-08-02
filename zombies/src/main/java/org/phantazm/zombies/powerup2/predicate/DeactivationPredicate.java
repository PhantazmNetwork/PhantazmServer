package org.phantazm.zombies.powerup2.predicate;


public interface DeactivationPredicate {
    void activate(long time);

    boolean shouldDeactivate(long time);
}
