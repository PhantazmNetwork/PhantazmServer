package org.phantazm.zombies.map;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.map.action.Action;

import java.util.List;

public class Wave {
    private final long delayTicks;
    private final int mobCount;
    private final List<Action<List<Mob>>> spawnActions;
    private final List<SpawnInfo> spawns;

    public Wave(long delayTicks, @NotNull List<Action<List<Mob>>> spawnActions,
        @NotNull List<SpawnInfo> spawns) {
        this.delayTicks = delayTicks;
        this.spawnActions = List.copyOf(spawnActions);
        this.spawns = List.copyOf(spawns);

        int count = 0;
        for (SpawnInfo info : spawns) {
            count += info.amount();
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

    public long delayTicks() {
        return delayTicks;
    }

    public @NotNull List<SpawnInfo> spawns() {
        return spawns;
    }

    public void onSpawn(@NotNull List<Mob> mobs) {
        for (Action<List<Mob>> action : spawnActions) {
            action.perform(mobs);
        }
    }
}
