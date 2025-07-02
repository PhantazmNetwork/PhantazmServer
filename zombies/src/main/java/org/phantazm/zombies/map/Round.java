package org.phantazm.zombies.map;

import net.minestom.server.Tickable;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.play.PluginMessagePacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.AttributeUtils;
import org.phantazm.core.packet.MinestomPacketUtils;
import org.phantazm.messaging.packet.server.RoundStartPacket;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.MobData;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.event.RoundStartEvent;
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
        @NotNull List<Action<Round>> endActions, @NotNull Supplier<ZombiesScene> sceneSupplier) {
        this.round = round;
        this.waves = List.copyOf(waves);
        this.startActions = List.copyOf(startActions);
        this.endActions = List.copyOf(endActions);

        this.spawnedMobs = new ConcurrentHashMap<>();

        this.totalMobCount = 0;

        this.sceneSupplier = Objects.requireNonNull(sceneSupplier);
    }

    public int round() {
        return round;
    }

    public void removeMob(@NotNull Mob mob) {
        if (spawnedMobs.remove(mob.getUuid()) != null && mob.data().extra()
            .getBooleanOrDefault(ExtraNodeKeys.PART_OF_ROUND, true)) {
            totalMobCount--;
        }
    }

    public void addMob(@NotNull Mob mob) {
        if (spawnedMobs.put(mob.getUuid(), mob) == null && mob.data().extra()
            .getBooleanOrDefault(ExtraNodeKeys.PART_OF_ROUND, true)) {
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

    private static int actualSpawnsForWave(SpawnDistributor distributor, Wave wave) {
        int amount = 0;
        for (SpawnInfo info : wave.spawns()) {
            MobData data = distributor.mobSpawner().dataForType(info.id());
            if (data == null || data.extra().getBooleanOrDefault(ExtraNodeKeys.PART_OF_ROUND, true)) {
                amount += info.amount();
            }
        }

        return amount;
    }

    public void startRound() {
        if (isActive) return;
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
        sceneSupplier.get().broadcastEvent(new RoundStartEvent(round));

        if (waves.isEmpty()) {
            for (Mob carryover : spawnedMobs.values()) {
                carryover.scheduler().scheduleNextTick(carryover::kill);
            }

            spawnedMobs.clear();
            return;
        }

        currentWave = waves.get(waveIndex = 0);
        waveTicks = 0;
        totalMobCount = 0;

        SpawnDistributor distributor = sceneSupplier.get().map().objects().spawnDistributor();
        int amount = 0;
        for (Wave wave : waves) {
            amount += actualSpawnsForWave(distributor, wave);
        }

        totalMobCount = amount;
    }

    public void endRound() {
        if (!isActive) return;

        isActive = false;
        for (Action<Round> action : endActions) {
            action.perform(this);
        }

        currentWave = null;
        waveTicks = 0;
        totalMobCount = 0;

        Iterator<Mob> spawnedIterator = spawnedMobs.values().iterator();
        List<Mob> preserved = null;
        while (spawnedIterator.hasNext()) {
            do {
                Mob mob = spawnedIterator.next();
                spawnedIterator.remove();

                if (mob.data().extra().getBooleanOrDefault(ExtraNodeKeys.KILL_AFTER_ROUND, true)) {
                    mob.kill();
                } else {
                    if (preserved == null) preserved = new ArrayList<>();
                    preserved.add(mob);
                }
            }
            while (spawnedIterator.hasNext());

            spawnedIterator = spawnedMobs.values().iterator();
        }

        if (preserved != null) {
            for (Mob mob : preserved) spawnedMobs.put(mob.getUuid(), mob);
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

        List<Mob> spawns = spawnDistributor.distributeSpawns(sceneSupplier.get().map().objects().spawnpoints(),
            spawnInfo);
        for (Mob spawn : spawns) {
            spawnedMobs.put(spawn.getUuid(), spawn);
        }

        int mobsSpawned = 0;
        for (Mob mob : spawns) {
            if (mob.data().extra().getBooleanOrDefault(ExtraNodeKeys.PART_OF_ROUND, true)) {
                mobsSpawned++;
            }
        }

        if (isWave) {
            //adjust for mobs that may have failed to spawn
            //only reached when calling internally
            totalMobCount -= actualSpawnsForWave(spawnDistributor, currentWave) - mobsSpawned;
        } else {
            totalMobCount += mobsSpawned;
        }

        return spawns;
    }

    @Override
    public void tick(long time) {
        if (!isActive) return;
        if (totalMobCount == 0) {
            endRound();
            return;
        }

        ++waveTicks;
        float actualWaveTicks = AttributeUtils.computeWithBase(currentWave.delayTicks(), sceneSupplier.get().map().roundHandler().waveDelayAttribute());
        if (waveIndex < waves.size() && waveTicks > actualWaveTicks) {
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
