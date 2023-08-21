package org.phantazm.zombies.map;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.map.action.Action;

import java.util.List;
import java.util.Objects;

public class Wave {
    private final WaveInfo waveInfo;
    private final int mobCount;
    private final List<Action<List<Mob>>> spawnActions;

    /**
     * Constructs a new instance of this class.
     *
     * @param waveInfo the backing data object
     */
    public Wave(@NotNull WaveInfo waveInfo, @NotNull List<Action<List<Mob>>> spawnActions) {
        this.waveInfo = Objects.requireNonNull(waveInfo);

        int count = 0;
        for (SpawnInfo spawnInfo : waveInfo.spawns()) {
            count += spawnInfo.amount();
        }
        this.mobCount = count;
        this.spawnActions = List.copyOf(spawnActions);
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

    public void onSpawn(@NotNull List<Mob> mobs) {
        for (Action<List<Mob>> action : spawnActions) {
            action.perform(mobs);
        }
    }
}
