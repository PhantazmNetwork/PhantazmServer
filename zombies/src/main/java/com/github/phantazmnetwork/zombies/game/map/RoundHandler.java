package com.github.phantazmnetwork.zombies.game.map;

import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;

public interface RoundHandler extends Tickable {
    int roundCount();

    int currentRoundIndex();

    void setCurrentRound(int roundIndex);

    Round currentRound();

    boolean hasEnded();
}
