package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.zombies.game.map.objects.MapObject;
import com.github.phantazmnetwork.zombies.map.SpawnInfo;
import com.github.phantazmnetwork.zombies.map.WaveInfo;
import org.jetbrains.annotations.NotNull;

public class Wave extends MapObject<WaveInfo> {
    private final int mobCount;

    /**
     * Constructs a new instance of this class.
     *
     * @param waveInfo the backing data object
     */
    public Wave(@NotNull WaveInfo waveInfo) {
        super(waveInfo);
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
}
