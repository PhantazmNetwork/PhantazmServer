package com.github.phantazmnetwork.zombies.game;

import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.game.map.Spawnpoint;
import com.github.phantazmnetwork.zombies.map.SpawnInfo;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class BasicSpawnDistributor implements SpawnDistributor {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicSpawnDistributor.class);

    private final Function<Key, MobModel> modelFunction;
    private final Supplier<List<Spawnpoint>> candidateGenerator;
    private final Random random;

    public BasicSpawnDistributor(@NotNull Function<Key, MobModel> modelFunction,
                                 @NotNull Supplier<List<Spawnpoint>> candidateGenerator, @NotNull Random random) {
        this.modelFunction = Objects.requireNonNull(modelFunction, "modelFunction");
        this.candidateGenerator = Objects.requireNonNull(candidateGenerator, "candidateGenerator");
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public @NotNull List<PhantazmMob> distributeSpawns(@NotNull Collection<SpawnInfo> spawns) {
        List<Pair<MobModel, Key>> spawnList = new ArrayList<>();
        for (SpawnInfo spawnInfo : spawns) {
            Key id = spawnInfo.id();
            MobModel model = modelFunction.apply(id);
            if (model == null) {
                LOGGER.warn("Found unrecognized mob type {}", id);
                continue;
            }

            for (int i = 0; i < spawnInfo.amount(); i++) {
                spawnList.add(Pair.of(model, spawnInfo.spawnType()));
            }
        }

        if (spawnList.isEmpty()) {
            LOGGER.warn("Spawn distributor received empty spawn list");
            return Collections.emptyList();
        }

        List<Spawnpoint> candidates = candidateGenerator.get();
        if (candidates == null || candidates.isEmpty()) {
            LOGGER.warn("Spawnpoint candidate generator returned a null or empty list");
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
            for (int j = 0; j < candidates.size(); j++) {
                Spawnpoint candidate = candidates.get(candidateIndex++);
                candidateIndex %= candidates.size();

                if (candidate.canSpawn(model, spawnType)) {
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
