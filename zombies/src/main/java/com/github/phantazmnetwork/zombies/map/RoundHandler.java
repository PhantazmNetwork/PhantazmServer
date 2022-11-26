package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.Tickable;

public interface RoundHandler extends Tickable {
    int roundCount();

    int currentRoundIndex();

    void setCurrentRound(int roundIndex);

    Round currentRound();

    boolean hasEnded();
}
