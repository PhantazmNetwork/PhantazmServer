package org.phantazm.zombies.map.handler;

import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Round;

import java.util.Optional;

public interface RoundHandler extends Tickable {
    int roundCount();

    int currentRoundIndex();

    void setCurrentRound(int roundIndex);

    @NotNull
    Optional<Round> currentRound();

    boolean hasEnded();

    void end();

    boolean isEndless();

    void enableEndless();
}
