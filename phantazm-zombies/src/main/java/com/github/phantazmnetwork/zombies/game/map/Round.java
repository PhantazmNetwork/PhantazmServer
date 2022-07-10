package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.game.RoundAction;
import com.github.phantazmnetwork.zombies.game.SpawnDistributor;
import com.github.phantazmnetwork.zombies.map.RoundInfo;
import com.github.phantazmnetwork.zombies.map.WaveInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

public class Round extends MapObject<RoundInfo> implements Tickable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Round.class);

    private final List<Wave> unmodifiableWaves;
    private final List<RoundAction> startActions;
    private final List<RoundAction> endActions;
    private final SpawnDistributor spawnDistributor;

    private boolean isActive;

    private long waveStartTime;
    private long time;

    private Wave currentWave;
    private int waveIndex;

    private int mobsRemaining;

    /**
     * Constructs a new instance of this class.
     *
     * @param roundInfo the backing data object
     */
    public Round(@NotNull RoundInfo roundInfo, @NotNull List<RoundAction> startActions,
                 @NotNull List<RoundAction> endActions, @NotNull SpawnDistributor spawnDistributor) {
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

        this.startActions.sort(Comparator.comparingInt(RoundAction::priority));
        this.endActions.sort(Comparator.comparingInt(RoundAction::priority));
    }

    public @UnmodifiableView @NotNull List<Wave> getWaves() {
        return unmodifiableWaves;
    }

    public int getMobsRemaining() {
        return mobsRemaining;
    }

    public void incrementMobsRemaining() {
        mobsRemaining++;
    }

    public void decrementMobsRemaining() {
        if(--mobsRemaining < 0) {
            throw new IllegalArgumentException("Cannot have negative mobs remaining");
        }
    }

    public void setMobsRemaining(int mobsRemaining) {
        if(mobsRemaining < 0) {
            throw new IllegalArgumentException("Cannot have negative mobs remaining");
        }

        this.mobsRemaining = mobsRemaining;
    }

    public boolean isActive() {
        return isActive;
    }

    public void startRound() {
        if(isActive) {
            return;
        }

        isActive = true;
        for(RoundAction action : startActions) {
            action.perform(this);
        }

        if(unmodifiableWaves.size() > 0) {
            waveStartTime = time;
            currentWave = unmodifiableWaves.get(waveIndex = 0);

            for(Wave wave : unmodifiableWaves) {
                mobsRemaining += wave.data.spawns().size();
            }
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
        for(RoundAction action : endActions) {
            action.perform(this);
        }

        waveStartTime = 0;
        currentWave = null;
        mobsRemaining = 0;
    }

    @Override
    public void tick(long time) {
        this.time = time;

        if(isActive) {
            if(mobsRemaining == 0) {
                endRound();
                return;
            }

            long timeSinceLastWave = time - waveStartTime;
            if(waveIndex < unmodifiableWaves.size() && timeSinceLastWave > currentWave.data.delayTicks()) {
                spawnDistributor.distributeSpawns(currentWave.data.spawns());

                waveStartTime = time;
                if(++waveIndex >= unmodifiableWaves.size()) {
                    return;
                }

                currentWave = unmodifiableWaves.get(waveIndex);
            }
        }
    }
}
