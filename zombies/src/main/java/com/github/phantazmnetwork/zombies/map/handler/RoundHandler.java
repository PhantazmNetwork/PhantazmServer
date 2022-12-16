package com.github.phantazmnetwork.zombies.map.handler;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.map.Round;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface RoundHandler extends Tickable {
    int roundCount();

    int currentRoundIndex();

    void setCurrentRound(int roundIndex);

    @NotNull Optional<Round> currentRound();

    boolean hasEnded();
}
