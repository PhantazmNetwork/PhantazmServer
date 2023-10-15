package org.phantazm.zombies.spawn;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.MobSpawner;
import org.phantazm.zombies.map.SpawnInfo;
import org.phantazm.zombies.map.Spawnpoint;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class BasicSpawnDistributor implements SpawnDistributor {
    private static final int SPAWN_LIMIT = 250;

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicSpawnDistributor.class);

    private final MobSpawner spawner;
    private final Supplier<ZombiesScene> zombiesScene;

    public BasicSpawnDistributor(@NotNull MobSpawner spawner, @NotNull Supplier<ZombiesScene> zombiesScene) {
        this.spawner = Objects.requireNonNull(spawner);
        this.zombiesScene = Objects.requireNonNull(zombiesScene);
    }

    @Override
    public @NotNull List<Mob> distributeSpawns(@NotNull List<? extends Spawnpoint> spawnpoints,
        @NotNull Collection<? extends SpawnInfo> spawns) {
        if (spawnpoints.isEmpty()) {
            return List.of();
        }

        ZombiesScene zombiesScene = this.zombiesScene.get();
        List<Pair<Key, Key>> spawnList = new ArrayList<>(spawns.size());
        for (SpawnInfo spawnInfo : spawns) {
            Key id = spawnInfo.id();
            if (!spawner.canSpawn(id)) {
                LOGGER.warn("Found unrecognized mob type '{}'", id);
                continue;
            }

            for (int i = 0; i < spawnInfo.amount(); i++) {
                spawnList.add(Pair.of(id, spawnInfo.spawnType()));
            }
        }

        if (spawnList.isEmpty()) {
            LOGGER.warn("Received empty spawn list");
            return List.of();
        }

        Collection<ZombiesPlayer> players = zombiesScene.managedPlayers().values();
        List<Spawnpoint> sortedSpawnpoints = new ArrayList<>(spawnpoints.size());
        for (Spawnpoint spawnpoint : spawnpoints) {
            if (spawnpoint.canSpawnAny(players)) {
                sortedSpawnpoints.add(spawnpoint);
            }
        }

        sortedSpawnpoints.sort((first, second) -> {
            double firstClosest = Double.POSITIVE_INFINITY;
            double secondClosest = Double.POSITIVE_INFINITY;
            for (ZombiesPlayer zombiesPlayer : players) {
                if (!zombiesPlayer.canTriggerSLA()) {
                    continue;
                }

                Optional<Player> playerOptional = zombiesPlayer.getPlayer();
                if (playerOptional.isEmpty()) {
                    continue;
                }

                Player player = playerOptional.get();

                double firstDistance = player.getDistance(first.spawnPoint());
                double secondDistance = player.getDistance(second.spawnPoint());

                if (firstDistance < firstClosest) {
                    firstClosest = firstDistance;
                }

                if (secondDistance < secondClosest) {
                    secondClosest = secondDistance;
                }
            }

            return Double.compare(firstClosest, secondClosest);
        });

        Collections.shuffle(spawnList, zombiesScene.map().objects().module().random());

        List<Mob> spawnedMobs = new ArrayList<>(spawnList.size());
        int candidateIndex = 0;
        for (int i = spawnList.size() - 1; i >= 0; i--) {
            Pair<Key, Key> spawnEntry = spawnList.get(i);
            Key spawnIdentifier = spawnEntry.first();
            Key spawnType = spawnEntry.second();

            boolean spawned = false;
            for (int j = 0; j < sortedSpawnpoints.size(); j++) {
                Spawnpoint candidate = sortedSpawnpoints.get(candidateIndex++);
                candidateIndex %= sortedSpawnpoints.size();

                if (candidate.canSpawn(spawnIdentifier, spawnType, players)) {
                    Mob mob = candidate.spawn(spawnIdentifier);
                    spawnedMobs.add(mob);
                    spawned = true;
                    break;
                }
            }

            if (!spawned) {
                LOGGER.warn("Found no suitable spawnpoint for mob {} using spawn type {}", spawnIdentifier, spawnType);
            }
        }

        return spawnedMobs;
    }
}
