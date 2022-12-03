package com.github.phantazmnetwork.zombies.equipment.gun.target;

import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.DirectionalEntityFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.HeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.intersectionfinder.IntersectionFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.limiter.TargetLimiter;
import com.github.phantazmnetwork.zombies.equipment.gun.target.tester.TargetTester;
import com.github.steanky.element.core.annotation.*;
import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Basic implementation of a {@link TargetFinder}.
 */
@Model("zombies.gun.target_finder.basic")
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
    @FactoryMethod
    public BasicTargetFinder(@NotNull Data data,
            @NotNull @DataName("entity_finder") DirectionalEntityFinder entityFinder,
            @NotNull @DataName("target_tester") TargetTester targetTester,
            @NotNull @DataName("intersection_finder") IntersectionFinder intersectionFinder,
            @NotNull @DataName("headshot_tester") HeadshotTester headshotTester,
            @NotNull @DataName("target_limiter") TargetLimiter targetLimiter) {
        this.entityFinder = Objects.requireNonNull(entityFinder, "entityFinder");
        this.targetTester = Objects.requireNonNull(targetTester, "targetTester");
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
     * @param entityFinderPath       A path to the {@link BasicTargetFinder}'s {@link DirectionalEntityFinder}
     * @param targetTesterPath       A path to the {@link BasicTargetFinder}'s {@link TargetTester}
     * @param intersectionFinderPath A path to the {@link BasicTargetFinder}'s {@link IntersectionFinder}
     * @param headshotTesterPath     A path to the {@link BasicTargetFinder}'s {@link HeadshotTester}
     * @param targetLimiterPath      A path to the {@link BasicTargetFinder}'s {@link TargetLimiter}
     */
    @DataObject
    public record Data(@NotNull @DataPath("entity_finder") String entityFinderPath,
                       @NotNull @DataPath("target_tester") String targetTesterPath,
                       @NotNull @DataPath("intersection_finder") String intersectionFinderPath,
                       @NotNull @DataPath("headshot_tester") String headshotTesterPath,
                       @NotNull @DataPath("target_limiter") String targetLimiterPath) {

        /**
         * Creates a {@link Data}.
         *
         * @param entityFinderPath       A path to the {@link BasicTargetFinder}'s {@link DirectionalEntityFinder}
         * @param targetTesterPath       A path to the {@link BasicTargetFinder}'s {@link TargetTester}
         * @param intersectionFinderPath A path to the {@link BasicTargetFinder}'s {@link IntersectionFinder}
         * @param headshotTesterPath     A path to the {@link BasicTargetFinder}'s {@link HeadshotTester}
         * @param targetLimiterPath      A path to the {@link BasicTargetFinder}'s {@link TargetLimiter}
         */
        public Data {
            Objects.requireNonNull(entityFinderPath, "entityFinderPath");
            Objects.requireNonNull(targetTesterPath, "targetTesterPath");
            Objects.requireNonNull(intersectionFinderPath, "intersectionFinderPath");
            Objects.requireNonNull(headshotTesterPath, "headshotTesterPath");
            Objects.requireNonNull(targetLimiterPath, "targetLimiterPath");
        }

    }

}
