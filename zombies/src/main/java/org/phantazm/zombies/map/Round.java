package org.phantazm.zombies.map;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.Tickable;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.spawn.SpawnDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Round implements Tickable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Round.class);

    private final RoundInfo roundInfo;
    private final List<Wave> waves;
    private final List<Action<Round>> startActions;
    private final List<Action<Round>> endActions;
    private final SpawnDistributor spawnDistributor;
    private final List<Spawnpoint> spawnpoints;
    private final Map<UUID, PhantazmMob> spawnedMobs;
    private final Collection<? extends ZombiesPlayer> zombiesPlayers;

    private boolean isActive;
    private long waveStartTime;
    private Wave currentWave;
    private int waveIndex;
    private int totalMobCount;

    /**
     * Constructs a new instance of this class.
     *
     * @param roundInfo the backing data object
     */
    public Round(@NotNull RoundInfo roundInfo, @NotNull List<Wave> waves, @NotNull List<Action<Round>> startActions,
            @NotNull List<Action<Round>> endActions, @NotNull SpawnDistributor spawnDistributor,
            @NotNull List<Spawnpoint> spawnpoints, @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers) {
        List<WaveInfo> waveInfo = roundInfo.waves();
        if (waveInfo.isEmpty()) {
            LOGGER.warn("Round {} has no waves", roundInfo);
        }

        this.roundInfo = Objects.requireNonNull(roundInfo, "roundInfo");
        this.waves = List.copyOf(waves);
        this.startActions = List.copyOf(startActions);
        this.endActions = List.copyOf(endActions);
        this.spawnDistributor = Objects.requireNonNull(spawnDistributor, "spawnDistributor");

        this.spawnedMobs = new HashMap<>();
        this.spawnpoints = Objects.requireNonNull(spawnpoints, "spawnpoints");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
    }

    public @NotNull RoundInfo getRoundInfo() {
        return roundInfo;
    }

    public void removeMob(@NotNull PhantazmMob mob) {
        if (spawnedMobs.remove(mob.entity().getUuid()) != null) {
            totalMobCount--;
        }
    }

    public void addMob(@NotNull PhantazmMob mob) {
        if (spawnedMobs.put(mob.entity().getUuid(), mob) == null) {
            totalMobCount++;
        }
    }

    public @Unmodifiable @NotNull List<PhantazmMob> getSpawnedMobs() {
        return List.copyOf(spawnedMobs.values());
    }

    public int getTotalMobCount() {
        return totalMobCount;
    }

    public @Unmodifiable @NotNull List<Wave> getWaves() {
        return waves;
    }

    public boolean isActive() {
        return isActive;
    }

    public void startRound(long time) {
        if (isActive) {
            return;
        }

        isActive = true;

        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (zombiesPlayer.hasQuit()) {
                continue;
            }

            int prevBestRound = zombiesPlayer.module().getStats().getBestRound();
            zombiesPlayer.module().getStats().setBestRound(Math.max(prevBestRound, roundInfo.round()));
        }

        for (Action<Round> action : startActions) {
            action.perform(this);
        }

        if (waves.size() > 0) {
            currentWave = waves.get(waveIndex = 0);
            waveStartTime = time;

            totalMobCount = 0;
            for (Wave wave : waves) {
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

        Iterator<PhantazmMob> spawnedIterator = spawnedMobs.values().iterator();
        while (spawnedIterator.hasNext()) {
            PhantazmMob mob = spawnedIterator.next();
            spawnedIterator.remove();

            mob.entity().kill();
        }
    }

    public @NotNull List<PhantazmMob> spawnMobs(@NotNull List<SpawnInfo> spawnInfo) {
        return spawnMobs(spawnInfo, spawnDistributor, false);
    }

    public boolean hasMob(@NotNull UUID uuid) {
        return spawnedMobs.containsKey(uuid);
    }

    private @NotNull List<PhantazmMob> spawnMobs(@NotNull List<SpawnInfo> spawnInfo,
            @NotNull SpawnDistributor spawnDistributor, boolean isWave) {
        if (!isActive) {
            throw new IllegalStateException("Round must be active to spawn mobs");
        }

        List<PhantazmMob> spawns = spawnDistributor.distributeSpawns(spawnpoints, spawnInfo);
        for (PhantazmMob spawn : spawns) {
            spawnedMobs.put(spawn.entity().getUuid(), spawn);
        }

        if (isWave) {
            //adjust for mobs that may have failed to spawn
            //only reached when calling internally
            totalMobCount -= currentWave.mobCount() - spawns.size();
        }
        else {
            totalMobCount += spawns.size();
        }

        return spawns;
    }

    @Override
    public void tick(long time) {
        if (isActive) {
            if (totalMobCount == 0) {
                endRound();
                return;
            }

            long timeSinceLastWave = (time - waveStartTime) / MinecraftServer.TICK_MS;
            if (waveIndex < waves.size() && timeSinceLastWave > currentWave.getWaveInfo().delayTicks()) {
                List<PhantazmMob> mobs = spawnMobs(currentWave.getWaveInfo().spawns(), spawnDistributor, true);
                currentWave.onSpawn(mobs);

                waveStartTime = time;
                if (++waveIndex >= waves.size()) {
                    return;
                }

                currentWave = waves.get(waveIndex);
            }
        }
    }
}
