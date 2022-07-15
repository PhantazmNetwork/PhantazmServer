package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.game.SpawnDistributor;
import com.github.phantazmnetwork.zombies.game.map.action.Action;
import com.github.phantazmnetwork.zombies.map.RoundInfo;
import com.github.phantazmnetwork.zombies.map.WaveInfo;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Round extends InstanceMapObject<RoundInfo> implements Tickable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Round.class);

    private final List<Wave> unmodifiableWaves;
    private final List<Action<Round>> startActions;
    private final List<Action<Round>> endActions;
    private final SpawnDistributor spawnDistributor;
    private final List<PhantazmMob> spawnedMobs;
    private final List<PhantazmMob> unmodifiableSpawnedMobs;
    private boolean isActive;
    private long waveStartTime;
    private long time;
    private Wave currentWave;
    private int waveIndex;
    private int totalMobCount;

    /**
     * Constructs a new instance of this class.
     *
     * @param roundInfo the backing data object
     */
    public Round(@NotNull RoundInfo roundInfo, @NotNull Instance instance, @NotNull List<Action<Round>> startActions,
                 @NotNull List<Action<Round>> endActions, @NotNull SpawnDistributor spawnDistributor) {
        super(roundInfo, instance);
        List<WaveInfo> waveInfo = roundInfo.waves();
        if (waveInfo.size() == 0) {
            LOGGER.warn("Round {} has no waves", roundInfo.round());
        }

        List<Wave> waves = new ArrayList<>(waveInfo.size());
        for (WaveInfo info : waveInfo) {
            waves.add(new Wave(info));
        }

        this.unmodifiableWaves = Collections.unmodifiableList(waves);
        this.startActions = Objects.requireNonNull(startActions, "startActions");
        this.endActions = Objects.requireNonNull(endActions, "endActions");
        this.spawnDistributor = Objects.requireNonNull(spawnDistributor, "spawnHandler");
        this.spawnedMobs = new ArrayList<>();
        this.unmodifiableSpawnedMobs = Collections.unmodifiableList(spawnedMobs);

        startActions.sort(Comparator.reverseOrder());
        endActions.sort(Comparator.reverseOrder());
    }

    public void removeMob(@NotNull PhantazmMob mob) {
        if (spawnedMobs.remove(mob)) {
            totalMobCount--;
        }
    }

    public @UnmodifiableView @NotNull List<PhantazmMob> getSpawnedMobs() {
        return unmodifiableSpawnedMobs;
    }

    public @UnmodifiableView @NotNull List<Wave> getWaves() {
        return unmodifiableWaves;
    }

    public boolean isActive() {
        return isActive;
    }

    public void startRound() {
        if (isActive) {
            return;
        }

        isActive = true;
        for (Action<Round> action : startActions) {
            action.perform(this);
        }

        if (unmodifiableWaves.size() > 0) {
            currentWave = unmodifiableWaves.get(waveIndex = 0);
            waveStartTime = time;

            totalMobCount = 0;
            for (Wave wave : unmodifiableWaves) {
                totalMobCount += wave.mobCount();
            }
        }
        else {
            endRound();
        }
    }

    public void endRound() {
        if (!isActive) {
            return;
        }

        isActive = false;
        for (Action<Round> action : endActions) {
            action.perform(this);
        }

        currentWave = null;
        waveStartTime = 0;
        totalMobCount = 0;

        spawnedMobs.clear();
    }

    @Override
    public void tick(long time) {
        this.time = time;

        if (isActive) {
            if (totalMobCount == 0) {
                endRound();
                return;
            }

            long timeSinceLastWave = time - waveStartTime;
            if (waveIndex < unmodifiableWaves.size() && timeSinceLastWave > currentWave.data.delayTicks()) {
                List<PhantazmMob> spawns = spawnDistributor.distributeSpawns(currentWave.data.spawns());
                spawnedMobs.addAll(spawns);

                //adjust for mobs that may have failed to spawn this wave
                totalMobCount -= currentWave.mobCount() - spawns.size();

                waveStartTime = time;
                if (++waveIndex >= unmodifiableWaves.size()) {
                    return;
                }

                currentWave = unmodifiableWaves.get(waveIndex);
            }
        }
    }
}
