package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.game.SpawnDistributor;
import com.github.phantazmnetwork.zombies.game.map.action.Action;
import com.github.phantazmnetwork.zombies.map.RoundInfo;
import com.github.phantazmnetwork.zombies.map.SpawnInfo;
import com.github.phantazmnetwork.zombies.map.WaveInfo;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class Round extends InstanceMapObject<RoundInfo> implements Tickable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Round.class);

    private final List<Wave> unmodifiableWaves;
    private final List<Action<Round>> startActions;
    private final List<Action<Round>> endActions;
    private final SpawnDistributor spawnDistributor;
    private final Supplier<List<Spawnpoint>> spawnpointSupplier;
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
                 @NotNull List<Action<Round>> endActions, @NotNull SpawnDistributor spawnDistributor,
                 @NotNull Supplier<List<Spawnpoint>> spawnpointSupplier) {
        super(roundInfo, instance);
        List<WaveInfo> waveInfo = roundInfo.waves();
        if (waveInfo.size() == 0) {
            LOGGER.warn("Round {} has no waves", roundInfo);
        }

        List<Wave> waves = new ArrayList<>(waveInfo.size());
        for (WaveInfo info : waveInfo) {
            waves.add(new Wave(info));
        }

        this.unmodifiableWaves = Collections.unmodifiableList(waves);
        this.startActions = new ArrayList<>(startActions);
        this.endActions = new ArrayList<>(endActions);
        this.spawnDistributor = Objects.requireNonNull(spawnDistributor, "spawnDistributor");
        this.spawnedMobs = new ArrayList<>();
        this.unmodifiableSpawnedMobs = Collections.unmodifiableList(spawnedMobs);
        this.spawnpointSupplier = Objects.requireNonNull(spawnpointSupplier, "spawnpointSupplier");
        this.startActions.sort(Comparator.reverseOrder());
        this.endActions.sort(Comparator.reverseOrder());
    }

    public void removeMob(@NotNull PhantazmMob mob) {
        if (spawnedMobs.remove(mob)) {
            totalMobCount--;
        }
    }

    public @UnmodifiableView @NotNull List<PhantazmMob> getSpawnedMobs() {
        return unmodifiableSpawnedMobs;
    }

    public int getTotalMobCount() {
        return totalMobCount;
    }

    public void setTotalMobCount(int count) {
        this.totalMobCount = count;
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

    public @NotNull List<PhantazmMob> spawnMobs(@NotNull List<SpawnInfo> spawnInfo) {
        return spawnMobs(spawnInfo, spawnDistributor, false);
    }

    public @NotNull List<PhantazmMob> spawnMobs(@NotNull List<SpawnInfo> spawnInfo,
                                                @NotNull SpawnDistributor spawnDistributor, boolean syncCount) {
        List<PhantazmMob> spawns = spawnDistributor.distributeSpawns(spawnpointSupplier.get(), spawnInfo);
        spawnedMobs.addAll(spawns);

        if (syncCount) {
            //adjust for mobs that may have failed to spawn
            totalMobCount -= currentWave.mobCount() - spawns.size();
        }
        else {
            totalMobCount += spawns.size();
        }

        return spawns;
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
                spawnMobs(currentWave.data.spawns(), spawnDistributor, true);

                waveStartTime = time;
                if (++waveIndex >= unmodifiableWaves.size()) {
                    return;
                }

                currentWave = unmodifiableWaves.get(waveIndex);
            }
        }
    }
}
