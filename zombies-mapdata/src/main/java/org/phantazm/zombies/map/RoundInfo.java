package org.phantazm.zombies.map;

import com.github.steanky.ethylene.core.collection.ConfigList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Defines a round.
 */
public record RoundInfo(int round,
                        @NotNull ConfigList startActions,
                        @NotNull ConfigList endActions,
                        @NotNull List<WaveInfo> waves) {

    /**
     * Creates a new instance of this record.
     *
     * @param round what round it is; starts at 1
     * @param waves the waves that make up this round
     */
    public RoundInfo {
        Objects.requireNonNull(waves, "waves");
    }
}
