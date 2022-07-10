package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.zombies.map.WaveInfo;
import org.jetbrains.annotations.NotNull;

public class Wave extends MapObject<WaveInfo> {
    /**
     * Constructs a new instance of this class.
     *
     * @param waveInfo the backing data object
     */
    public Wave(@NotNull WaveInfo waveInfo) {
        super(waveInfo);
    }
}
