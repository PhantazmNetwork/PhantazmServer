package com.github.phantazmnetwork.zombies.game.spawn;

import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.game.map.Spawnpoint;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.map.SpawnInfo;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

public class BasicSpawnDistributor implements SpawnDistributor {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicSpawnDistributor.class);

    private final Function<? super Key, ? extends MobModel> modelFunction;
    private final Random random;

    private final Collection<ZombiesPlayer> zombiesPlayers;

    public BasicSpawnDistributor(@NotNull Function<? super Key, ? extends MobModel> modelFunction,
            @NotNull Random random, @NotNull Collection<ZombiesPlayer> zombiesPlayers) {
        this.modelFunction = Objects.requireNonNull(modelFunction, "modelFunction");
        this.random = Objects.requireNonNull(random, "random");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
    }

    @Override
    public @NotNull List<PhantazmMob> distributeSpawns(@NotNull List<? extends Spawnpoint> spawnpoints,
            @NotNull Collection<? extends SpawnInfo> spawns) {
        if (spawnpoints.isEmpty()) {
            return new ArrayList<>(0);
        }

        List<Pair<MobModel, Key>> spawnList = new ArrayList<>(spawns.size());
        for (SpawnInfo spawnInfo : spawns) {
            Key id = spawnInfo.id();
            MobModel model = modelFunction.apply(id);
            if (model == null) {
                LOGGER.warn("Found unrecognized mob type '{}'", id);
                continue;
            }

            for (int i = 0; i < spawnInfo.amount(); i++) {
                spawnList.add(Pair.of(model, spawnInfo.spawnType()));
            }
        }

        if (spawnList.isEmpty()) {
            LOGGER.warn("Received empty spawn list");
            return Collections.emptyList();
        }

        Collections.shuffle(spawnList, random);

        List<PhantazmMob> spawnedMobs = new ArrayList<>(spawnList.size());
        int candidateIndex = 0;
        for (int i = spawnList.size() - 1; i >= 0; i--) {
            Pair<MobModel, Key> spawnEntry = spawnList.get(i);
            MobModel model = spawnEntry.first();
            Key spawnType = spawnEntry.second();

            boolean spawned = false;
            for (int j = 0; j < spawnpoints.size(); j++) {
                Spawnpoint candidate = spawnpoints.get(candidateIndex++);
                candidateIndex %= spawnpoints.size();

                if (candidate.canSpawn(model, spawnType, zombiesPlayers)) {
                    spawnedMobs.add(candidate.spawn(model));
                    spawned = true;
                    break;
                }
            }

            if (!spawned) {
                LOGGER.warn("Found no suitable spawnpoint for mob {} using spawn type {}", model.key(), spawnType);
            }
        }

        return spawnedMobs;
    }
}
