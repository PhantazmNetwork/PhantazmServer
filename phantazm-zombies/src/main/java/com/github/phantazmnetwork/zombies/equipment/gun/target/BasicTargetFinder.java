package com.github.phantazmnetwork.zombies.equipment.gun.target;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.DirectionalEntityFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.HeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.tester.TargetTester;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.ToDoubleFunction;

public class BasicTargetFinder implements TargetFinder {

    public record Data(@NotNull Key finderKey, @NotNull Key targetTesterKey, @NotNull Key headshotTesterKey,
                       boolean ignorePreviousHits, int maxTargets)
            implements Keyed {

        public static final Key SERIAL_KEY = Key.key("gun.target_finder.basic");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor(@NotNull Collection<Key> requested) {
        Objects.requireNonNull(requested, "requested");

        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key finderKey = keyProcessor.dataFromElement(element.getElementOrThrow("finderKey"));
                Key targetTesterKey = keyProcessor.dataFromElement(element.getElementOrThrow("targetTesterKey"));
                Key headshotTesterKey = keyProcessor.dataFromElement(element.getElementOrThrow("headshotTesterKey"));

                requested.add(finderKey);
                requested.add(targetTesterKey);
                requested.add(headshotTesterKey);

                boolean ignorePreviousHits = element.getBooleanOrThrow("ignorePreviousHits");
                int maxTargets = element.getNumberOrThrow("maxTargets").intValue();

                return new Data(finderKey, targetTesterKey, headshotTesterKey, ignorePreviousHits, maxTargets);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(5);
                node.put("finderKey", keyProcessor.elementFromData(data.finderKey()));
                node.put("targetTesterKey", keyProcessor.elementFromData(data.targetTesterKey()));
                node.put("headshotTesterKey", keyProcessor.elementFromData(data.headshotTesterKey()));
                node.putBoolean("ignorePreviousHits", data.ignorePreviousHits());
                node.putNumber("maxTargets", data.maxTargets());

                return node;
            }
        };
    }

    private final Data data;

    private final MobStore store;

    private final DirectionalEntityFinder finder;

    private final TargetTester targetTester;

    private final HeadshotTester headshotTester;

    public BasicTargetFinder(@NotNull Data data, @NotNull MobStore store, @NotNull DirectionalEntityFinder finder,
                             @NotNull TargetTester targetTester, @NotNull HeadshotTester headshotTester) {
        this.data = Objects.requireNonNull(data, "data");
        this.store = Objects.requireNonNull(store, "store");
        this.targetTester = Objects.requireNonNull(targetTester, "targetTester");
        this.finder = Objects.requireNonNull(finder, "finder");
        this.headshotTester = Objects.requireNonNull(headshotTester, "headshotTester");
    }

    @Override
    public @NotNull Result findTarget(@NotNull Player player, @NotNull Pos start, @NotNull Point end,
                                      @NotNull Collection<PhantazmMob> previousHits) {
        Instance instance = player.getInstance();
        if (instance == null) {
            return new Result(Collections.emptyList(), Collections.emptyList());
        }

        Collection<Entity> nearbyEntities = finder.findEntities(instance, start, end);
        List<Pair<PhantazmMob, Vec>> locations = new ArrayList<>(nearbyEntities.size());
        for (Entity entity : nearbyEntities) {
            PhantazmMob mob = store.getMob(entity.getUuid());
            if (mob != null && !(data.ignorePreviousHits() && previousHits.contains(mob))) {
                targetTester.getHitLocation(player, mob, start).ifPresent(intersection -> {
                    locations.add(Pair.of(mob, intersection));
                });
            }
        }

        locations.sort(Comparator.comparingDouble(new ToDoubleFunction<>() {

            private final Object2DoubleMap<UUID> distanceMap = new Object2DoubleOpenHashMap<>(locations.size());

            @Override
            public double applyAsDouble(Pair<PhantazmMob, Vec> value) {
                return distanceMap.computeIfAbsent(value.left().entity().getUuid(),
                        unused -> value.right().distanceSquared(start));
            }
        }));
        List<Pair<PhantazmMob, Vec>> adjustedLocations = locations.subList(0, Math.min(locations.size(),
                data.maxTargets()));

        Collection<GunHit> targets = new ArrayList<>(adjustedLocations.size());
        Collection<GunHit> headshots = new ArrayList<>(adjustedLocations.size());
        for (Pair<PhantazmMob, Vec> pair : adjustedLocations) {
            if (headshotTester.isHeadshot(player, pair.left(), pair.right())) {
                headshots.add(new GunHit(pair.left(), pair.right()));
            } else {
                targets.add(new GunHit(pair.left(), pair.right()));
            }
            previousHits.add(pair.left());
        }

        return new Result(targets, headshots);
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }

}
