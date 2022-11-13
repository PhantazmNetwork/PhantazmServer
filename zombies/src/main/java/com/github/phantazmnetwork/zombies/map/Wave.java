package com.github.phantazmnetwork.zombies.map;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Wave {

    private final WaveInfo waveInfo;

    private final int mobCount;

    /**
     * Constructs a new instance of this class.
     *
     * @param waveInfo the backing data object
     */
    public Wave(@NotNull WaveInfo waveInfo) {
        this.waveInfo = Objects.requireNonNull(waveInfo, "waveInfo");

        int count = 0;
        for (SpawnInfo spawnInfo : waveInfo.spawns()) {
            count += spawnInfo.amount();
        }
        this.mobCount = count;
    }

    /**
     * Gets the total number of mobs that should spawn this wave.
     *
     * @return the total number of mobs that should spawn this wave
     */
    public int mobCount() {
        return mobCount;
    }

    public @NotNull WaveInfo getWaveInfo() {
        return waveInfo;
    }
}
