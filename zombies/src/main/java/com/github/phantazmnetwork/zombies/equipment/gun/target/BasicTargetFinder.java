package com.github.phantazmnetwork.zombies.equipment.gun.target;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.DirectionalEntityFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.HeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.intersectionfinder.IntersectionFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.limiter.TargetLimiter;
import com.github.phantazmnetwork.zombies.equipment.gun.target.tester.TargetTester;
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

/**
 * Basic implementation of a {@link TargetFinder}.
 */
public class BasicTargetFinder implements TargetFinder {

    private final DirectionalEntityFinder entityFinder;
    private final TargetTester targetTester;
    private final IntersectionFinder intersectionFinder;
    private final HeadshotTester headshotTester;
    private final TargetLimiter targetLimiter;

    /**
     * Creates a new {@link BasicTargetFinder}.
     *
     * @param entityFinder       A {@link DirectionalEntityFinder} which finds potential entities to become targets
     * @param targetTester       A {@link TargetTester} which tests if an entity should become a target
     * @param intersectionFinder A {@link IntersectionFinder} which finds the intersection of a target with the shot
     * @param headshotTester     A {@link HeadshotTester} which tests if a target should be headshotted
     * @param targetLimiter      A {@link TargetLimiter} which limits the number of targets found
     */
    public BasicTargetFinder(@NotNull DirectionalEntityFinder entityFinder, @NotNull TargetTester targetTester,
                             @NotNull IntersectionFinder intersectionFinder, @NotNull HeadshotTester headshotTester,
                             @NotNull TargetLimiter targetLimiter) {
        this.entityFinder = Objects.requireNonNull(entityFinder, "entityFinder");
        this.targetTester = Objects.requireNonNull(targetTester, "targetTester");
        this.intersectionFinder = Objects.requireNonNull(intersectionFinder, "intersectionFinder");
        this.headshotTester = Objects.requireNonNull(headshotTester, "headshotTester");
        this.targetLimiter = Objects.requireNonNull(targetLimiter, "targetLimiter");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key entityFinderKey = keyProcessor.dataFromElement(element.getElementOrThrow("entityFinderKey"));
                Key targetTesterKey = keyProcessor.dataFromElement(element.getElementOrThrow("targetTesterKey"));
                Key intersectionFinderKey =
                        keyProcessor.dataFromElement(element.getElementOrThrow("intersectionFinderKey"));
                Key headshotTesterKey = keyProcessor.dataFromElement(element.getElementOrThrow("headshotTesterKey"));
                Key targetLimiterKey = keyProcessor.dataFromElement(element.getElementOrThrow("targetLimiterKey"));

                return new Data(entityFinderKey, targetTesterKey, intersectionFinderKey, headshotTesterKey,
                                targetLimiterKey
                );
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(5);
                node.put("entityFinderKey", keyProcessor.elementFromData(data.entityFinderKey()));
                node.put("targetTesterKey", keyProcessor.elementFromData(data.targetTesterKey()));
                node.put("intersectionFinderKey", keyProcessor.elementFromData(data.intersectionFinderKey()));
                node.put("headshotTesterKey", keyProcessor.elementFromData(data.headshotTesterKey()));
                node.put("targetLimiterKey", keyProcessor.elementFromData(data.targetLimiterKey()));

                return node;
            }
        };
    }

    /**
     * Creates a dependency consumer for {@link Data}s.
     *
     * @return A dependency consumer for {@link Data}s
     */
    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> {
            keys.add(data.entityFinderKey());
            keys.add(data.intersectionFinderKey());
            keys.add(data.headshotTesterKey());
            keys.add(data.targetLimiterKey());
        };
    }

    @Override
    public @NotNull Result findTarget(@NotNull Entity shooter, @NotNull Pos start, @NotNull Point end,
                                      @NotNull Collection<UUID> previousHits) {
        Instance instance = shooter.getInstance();
        if (instance == null) {
            return new Result(Collections.emptyList(), Collections.emptyList());
        }

        Collection<LivingEntity> nearbyEntities = entityFinder.findEntities(instance, start, end);
        List<Pair<? extends LivingEntity, Vec>> locations = new ArrayList<>(nearbyEntities.size());
        for (LivingEntity entity : nearbyEntities) {
            if (targetTester.useTarget(entity, previousHits)) {
                intersectionFinder.getHitLocation(entity, start)
                                  .ifPresent(intersection -> locations.add(Pair.of(entity, intersection)));
            }
        }
        List<Pair<? extends LivingEntity, Vec>> adjustedLocations = targetLimiter.limitTargets(start, locations);

        Collection<GunHit> targets = new ArrayList<>(locations.size());
        Collection<GunHit> headshots = new ArrayList<>(adjustedLocations.size());
        for (Pair<? extends LivingEntity, Vec> pair : adjustedLocations) {
            if (headshotTester.isHeadshot(shooter, pair.left(), pair.right())) {
                headshots.add(new GunHit(pair.left(), pair.right()));
            }
            else {
                targets.add(new GunHit(pair.left(), pair.right()));
            }
            previousHits.add(pair.left().getUuid());
        }

        return new Result(targets, headshots);
    }

    /**
     * Data for a {@link BasicTargetFinder}.
     *
     * @param entityFinderKey       A {@link Key} to the {@link BasicTargetFinder}'s {@link DirectionalEntityFinder}
     * @param targetTesterKey       A {@link Key} to the {@link BasicTargetFinder}'s {@link TargetTester}
     * @param intersectionFinderKey A {@link Key} to the {@link BasicTargetFinder}'s {@link IntersectionFinder}
     * @param headshotTesterKey     A {@link Key} to the {@link BasicTargetFinder}'s {@link HeadshotTester}
     * @param targetLimiterKey      A {@link Key} to the {@link BasicTargetFinder}'s {@link TargetLimiter}
     */
    public record Data(@NotNull Key entityFinderKey,
                       @NotNull Key targetTesterKey,
                       @NotNull Key intersectionFinderKey,
                       @NotNull Key headshotTesterKey,
                       @NotNull Key targetLimiterKey) implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.target_finder.basic");

        /**
         * Creates a {@link Data}.
         *
         * @param entityFinderKey       A {@link Key} to the {@link BasicTargetFinder}'s {@link DirectionalEntityFinder}
         * @param targetTesterKey       A {@link Key} to the {@link BasicTargetFinder}'s {@link TargetTester}
         * @param intersectionFinderKey A {@link Key} to the {@link BasicTargetFinder}'s {@link IntersectionFinder}
         * @param headshotTesterKey     A {@link Key} to the {@link BasicTargetFinder}'s {@link HeadshotTester}
         * @param targetLimiterKey      A {@link Key} to the {@link BasicTargetFinder}'s {@link TargetLimiter}
         */
        public Data {
            Objects.requireNonNull(entityFinderKey, "entityFinderKey");
            Objects.requireNonNull(targetTesterKey, "targetTesterKey");
            Objects.requireNonNull(intersectionFinderKey, "intersectionFinderKey");
            Objects.requireNonNull(headshotTesterKey, "headshotTesterKey");
            Objects.requireNonNull(targetLimiterKey, "targetLimiterKey");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

}
