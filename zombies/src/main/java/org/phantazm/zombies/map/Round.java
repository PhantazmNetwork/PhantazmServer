package org.phantazm.zombies.map;

import net.minestom.server.Tickable;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.PluginMessagePacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.packet.MinestomPacketUtils;
import org.phantazm.messaging.packet.server.RoundStartPacket;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.phantazm.zombies.spawn.SpawnDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class Round implements Tickable {
    private static final CachedPacket ROUND_START_PACKET = new CachedPacket(() ->
        new PluginMessagePacket(RoundStartPacket.ID.asString(), MinestomPacketUtils.serialize(new RoundStartPacket())));

    private final int round;
    private final List<Wave> waves;
    private final List<Action<Round>> startActions;
    private final List<Action<Round>> endActions;
    private final List<Spawnpoint> spawnpoints;
    private final Map<UUID, Mob> spawnedMobs;

    private final Supplier<ZombiesScene> sceneSupplier;

    private boolean isActive;
    private long waveTicks = 0;
    private Wave currentWave;
    private int waveIndex;
    private int totalMobCount;

    /**
     * Constructs a new instance of this class.
     */
    public Round(int round, @NotNull List<Wave> waves, @NotNull List<Action<Round>> startActions,
        @NotNull List<Action<Round>> endActions, @NotNull List<Spawnpoint> spawnpoints,
        @NotNull Supplier<ZombiesScene> sceneSupplier) {
        this.round = round;
        this.waves = List.copyOf(waves);
        this.startActions = List.copyOf(startActions);
        this.endActions = List.copyOf(endActions);

        this.spawnedMobs = new ConcurrentHashMap<>();
        this.spawnpoints = Objects.requireNonNull(spawnpoints);

        this.totalMobCount = 0;

        this.sceneSupplier = Objects.requireNonNull(sceneSupplier);
    }

    public int round() {
        return round;
    }

    public void removeMob(@NotNull Mob mob) {
        if (spawnedMobs.remove(mob.getUuid()) != null) {
            totalMobCount--;
        }
    }

    public void addMob(@NotNull Mob mob) {
        if (spawnedMobs.put(mob.getUuid(), mob) == null) {
            totalMobCount++;
        }
    }

    public @Unmodifiable
    @NotNull List<Mob> getSpawnedMobs() {
        return List.copyOf(spawnedMobs.values());
    }

    public int totalMobCount() {
        return totalMobCount;
    }

    public @Unmodifiable
    @NotNull List<Wave> getWaves() {
        return waves;
    }

    public boolean isActive() {
        return isActive;
    }

    public void startRound() {
        if (isActive) {
            return;
        }

        isActive = true;

        for (ZombiesPlayer zombiesPlayer : sceneSupplier.get().managedPlayers().values()) {
            if (zombiesPlayer.hasQuit()) {
                continue;
            }

            int prevBestRound = zombiesPlayer.module().getStats().getBestRound();
            zombiesPlayer.module().getStats().setBestRound(Math.max(prevBestRound, round));
            zombiesPlayer.getPlayer().ifPresent(player -> player.sendPacket(ROUND_START_PACKET));
        }

        for (Action<Round> action : startActions) {
            action.perform(this);
        }

        if (waves.isEmpty()) {
            endRound();
            return;
        }

        currentWave = waves.get(waveIndex = 0);
        waveTicks = 0;

        totalMobCount = 0;
        for (Wave wave : waves) {
            totalMobCount += wave.mobCount();
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
        waveTicks = 0;
        totalMobCount = 0;

        Iterator<Mob> spawnedIterator = spawnedMobs.values().iterator();
        while (spawnedIterator.hasNext()) {
            do {
                Mob mob = spawnedIterator.next();
                spawnedIterator.remove();
                mob.getAcquirable().sync(self -> ((LivingEntity) self).kill());
            }
            while (spawnedIterator.hasNext());

            spawnedIterator = spawnedMobs.values().iterator();
        }
    }

    public @NotNull List<Mob> spawnMobs(@NotNull List<SpawnInfo> spawnInfo) {
        return spawnMobs(spawnInfo, sceneSupplier.get().map().objects().spawnDistributor(), false);
    }

    public boolean hasMob(@NotNull UUID uuid) {
        return spawnedMobs.containsKey(uuid);
    }

    private @NotNull List<Mob> spawnMobs(@NotNull List<SpawnInfo> spawnInfo, @NotNull SpawnDistributor spawnDistributor,
        boolean isWave) {
        if (!isActive) {
            throw new IllegalStateException("Round must be active to spawn mobs");
        }

        List<Mob> spawns = spawnDistributor.distributeSpawns(spawnpoints, spawnInfo);
        for (Mob spawn : spawns) {
            spawnedMobs.put(spawn.getUuid(), spawn);
        }

        if (isWave) {
            //adjust for mobs that may have failed to spawn
            //only reached when calling internally
            totalMobCount -= currentWave.mobCount() - spawns.size();
        } else {
            totalMobCount += spawns.size();
        }

        return spawns;
    }

    @Override
    public void tick(long time) {
        if (!isActive) {
            return;
        }

        if (totalMobCount == 0) {
            endRound();
            return;
        }

        ++waveTicks;
        if (waveIndex < waves.size() && waveTicks > currentWave.delayTicks()) {
            List<Mob> mobs = spawnMobs(currentWave.spawns(), sceneSupplier.get().map().objects().spawnDistributor(), true);
            currentWave.onSpawn(mobs);

            waveTicks = 0;
            if (++waveIndex >= waves.size()) {
                return;
            }

            currentWave = waves.get(waveIndex);
        }
    }
}
