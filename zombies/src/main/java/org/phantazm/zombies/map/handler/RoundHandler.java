package org.phantazm.zombies.map.handler;

import net.minestom.server.Tickable;
import net.minestom.server.attribute.AttributeInstance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.endless.Endless;
import org.phantazm.zombies.map.Round;
import org.w3c.dom.Attr;

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

    @NotNull AttributeInstance waveDelayAttribute();

    @NotNull Optional<Endless> endless();
}
