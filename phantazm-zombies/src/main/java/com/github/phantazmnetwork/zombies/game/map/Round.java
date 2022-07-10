package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.game.SpawnDistributor;
import com.github.phantazmnetwork.zombies.game.map.action.Action;
import com.github.phantazmnetwork.zombies.map.RoundInfo;
import com.github.phantazmnetwork.zombies.map.WaveInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Round extends MapObject<RoundInfo> implements Tickable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Round.class);

    private final List<Wave> unmodifiableWaves;
    private final List<Action<Round>> startActions;
    private final List<Action<Round>> endActions;
    private final SpawnDistributor spawnDistributor;

    private boolean isActive;

    private long waveStartTime;
    private long time;

    private Wave currentWave;
    private int waveIndex;

    /**
     * Constructs a new instance of this class.
     *
     * @param roundInfo the backing data object
     */
    public Round(@NotNull RoundInfo roundInfo, @NotNull List<Action<Round>> startActions,
                 @NotNull List<Action<Round>> endActions,
                 @NotNull SpawnDistributor spawnDistributor) {
        super(roundInfo);
        List<WaveInfo> waveInfo = roundInfo.waves();
        if(waveInfo.size() == 0) {
            LOGGER.warn("Round {} has no waves", roundInfo.round());
        }

        List<Wave> waves = new ArrayList<>(waveInfo.size());
        for(WaveInfo info : waveInfo) {
            waves.add(new Wave(info));
        }

        this.unmodifiableWaves = Collections.unmodifiableList(waves);
        this.startActions = Objects.requireNonNull(startActions, "startActions");
        this.endActions = Objects.requireNonNull(endActions, "endActions");
        this.spawnDistributor = Objects.requireNonNull(spawnDistributor, "spawnHandler");

        Collections.sort(startActions);
        Collections.sort(endActions);
    }

    public @UnmodifiableView @NotNull List<Wave> getWaves() {
        return unmodifiableWaves;
    }

    public boolean isActive() {
        return isActive;
    }

    public void startRound() {
        if(isActive) {
            return;
        }

        isActive = true;
        for(Action<Round> action : startActions) {
            action.perform(this);
        }

        if(unmodifiableWaves.size() > 0) {
            waveStartTime = time;
            currentWave = unmodifiableWaves.get(waveIndex = 0);

            int expectedCount = 0;
            for(Wave wave : unmodifiableWaves) {
                expectedCount += wave.data.spawns().size();
            }

            //trackingMobSpawner.setCount(expectedCount);
        }
        else {
            endRound();
        }
    }

    public void endRound() {
        if(!isActive) {
            return;
        }

        isActive = false;
        for(Action<Round> action : endActions) {
            action.perform(this);
        }

        waveStartTime = 0;
        currentWave = null;
        //trackingMobSpawner.setCount(0);
    }

    @Override
    public void tick(long time) {
        this.time = time;

        if(isActive) {
            if(trackingMobSpawner.count() == 0) {
                endRound();
                return;
            }

            long timeSinceLastWave = time - waveStartTime;
            if(waveIndex < unmodifiableWaves.size() && timeSinceLastWave > currentWave.data.delayTicks()) {
                List<PhantazmMob> spawns = spawnDistributor.distributeSpawns(currentWave.data.spawns());
                trackingMobSpawner.setCount(trackingMobSpawner.count() - (currentWave.data.spawns().size() - spawns
                        .size()));

                waveStartTime = time;
                if(++waveIndex >= unmodifiableWaves.size()) {
                    return;
                }

                currentWave = unmodifiableWaves.get(waveIndex);
            }
        }
    }
}
