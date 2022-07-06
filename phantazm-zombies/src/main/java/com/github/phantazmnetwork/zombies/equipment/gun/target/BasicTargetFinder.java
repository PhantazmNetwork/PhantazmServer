package com.github.phantazmnetwork.zombies.equipment.gun.target;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.DirectionalEntityFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.HeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.intersectionfinder.IntersectionFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.limiter.TargetLimiter;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

public class BasicTargetFinder implements TargetFinder {

    public record Data(@NotNull Key finderKey, @NotNull Key targetTesterKey, @NotNull Key headshotTesterKey,
                       @NotNull Key targetLimiterKey, boolean ignorePreviousHits)
            implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.target_finder.basic");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key finderKey = keyProcessor.dataFromElement(element.getElementOrThrow("finderKey"));
                Key targetTesterKey = keyProcessor.dataFromElement(element.getElementOrThrow("targetTesterKey"));
                Key headshotTesterKey = keyProcessor.dataFromElement(element.getElementOrThrow("headshotTesterKey"));
                Key targetLimiterKey = keyProcessor.dataFromElement(element.getElementOrThrow("targetLimiterKey"));
                boolean ignorePreviousHits = element.getBooleanOrThrow("ignorePreviousHits");

                return new Data(finderKey, targetTesterKey, headshotTesterKey, targetLimiterKey, ignorePreviousHits);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(5);
                node.put("finderKey", keyProcessor.elementFromData(data.finderKey()));
                node.put("targetTesterKey", keyProcessor.elementFromData(data.targetTesterKey()));
                node.put("headshotTesterKey", keyProcessor.elementFromData(data.headshotTesterKey()));
                node.put("targetLimiterKey", keyProcessor.elementFromData(data.targetLimiterKey()));
                node.putBoolean("ignorePreviousHits", data.ignorePreviousHits());

                return node;
            }
        };
    }

    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> {
            keys.add(data.finderKey());
            keys.add(data.targetTesterKey());
            keys.add(data.headshotTesterKey());
        };
    }

    private final Data data;

    private final MobStore store;

    private final DirectionalEntityFinder finder;

    private final IntersectionFinder intersectionFinder;

    private final HeadshotTester headshotTester;

    private final TargetLimiter targetLimiter;

    public BasicTargetFinder(@NotNull Data data, @NotNull MobStore store, @NotNull DirectionalEntityFinder finder,
                             @NotNull IntersectionFinder intersectionFinder, @NotNull HeadshotTester headshotTester,
                             @NotNull TargetLimiter targetLimiter) {
        this.data = Objects.requireNonNull(data, "data");
        this.store = Objects.requireNonNull(store, "store");
        this.finder = Objects.requireNonNull(finder, "finder");
        this.intersectionFinder = Objects.requireNonNull(intersectionFinder, "intersectionFinder");
        this.headshotTester = Objects.requireNonNull(headshotTester, "headshotTester");
        this.targetLimiter = Objects.requireNonNull(targetLimiter, "targetLimiter");
    }

    @Override
    public @NotNull Result findTarget(@NotNull Entity shooter, @NotNull Pos start, @NotNull Point end,
                                      @NotNull Collection<UUID> previousHits) {
        Instance instance = shooter.getInstance();
        if (instance == null) {
            return new Result(Collections.emptyList(), Collections.emptyList());
        }

        Collection<LivingEntity> nearbyEntities = finder.findEntities(instance, start, end);
        List<Pair<? extends LivingEntity, Vec>> locations = new ArrayList<>(nearbyEntities.size());
        for (LivingEntity entity : nearbyEntities) {
            if (!(data.ignorePreviousHits() && previousHits.contains(entity.getUuid()))) {
                intersectionFinder.getHitLocation(entity, start).ifPresent(intersection -> {
                    locations.add(Pair.of(entity, intersection));
                });
            }
        }
        List<Pair<? extends LivingEntity, Vec>> adjustedLocations = targetLimiter.limitTargets(start, locations);

        Collection<GunHit> targets = new ArrayList<>(locations.size());
        Collection<GunHit> headshots = new ArrayList<>(adjustedLocations.size());
        for (Pair<? extends LivingEntity, Vec> pair : adjustedLocations) {
            if (headshotTester.isHeadshot(shooter, pair.left(), pair.right())) {
                headshots.add(new GunHit(pair.left(), pair.right()));
            } else {
                targets.add(new GunHit(pair.left(), pair.right()));
            }
            previousHits.add(pair.left().getUuid());
        }

        return new Result(targets, headshots);
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }

}
