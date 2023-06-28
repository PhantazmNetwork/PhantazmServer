package org.phantazm.zombies.map.handler;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;
import org.phantazm.zombies.map.Round;

import java.util.Optional;

public interface RoundHandler extends Tickable {
    int roundCount();

    int currentRoundIndex();

    void setCurrentRound(int roundIndex);

    @NotNull Optional<Round> currentRound();

    boolean hasEnded();

    void end();
}
