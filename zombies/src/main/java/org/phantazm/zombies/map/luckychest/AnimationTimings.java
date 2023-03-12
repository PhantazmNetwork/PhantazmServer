package org.phantazm.zombies.map.luckychest;

public interface AnimationTimings {
    void start(long time);

    boolean shouldAdvance(long time);
}
