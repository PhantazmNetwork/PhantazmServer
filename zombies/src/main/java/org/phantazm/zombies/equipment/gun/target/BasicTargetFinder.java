package org.phantazm.zombies.equipment.gun.target;

import com.github.steanky.element.core.annotation.*;
import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.gun.target.entityfinder.directional.DirectionalEntityFinder;
import org.phantazm.zombies.equipment.gun.target.headshot.HeadshotTester;
import org.phantazm.zombies.equipment.gun.target.intersectionfinder.IntersectionFinder;
import org.phantazm.zombies.equipment.gun.target.limiter.TargetLimiter;
import org.phantazm.zombies.equipment.gun.target.tester.TargetTester;

import java.util.*;

/**
 * Basic implementation of a {@link TargetFinder}.
 */
@Model("zombies.gun.target_finder.basic")
@Cache(false)
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
    public BasicTargetFinder(@NotNull @Child("entity_finder") DirectionalEntityFinder entityFinder,
        @NotNull @Child("target_tester") TargetTester targetTester,
        @NotNull @Child("intersection_finder") IntersectionFinder intersectionFinder,
        @NotNull @Child("headshot_tester") HeadshotTester headshotTester,
        @NotNull @Child("target_limiter") TargetLimiter targetLimiter) {
        this.entityFinder = Objects.requireNonNull(entityFinder);
        this.targetTester = Objects.requireNonNull(targetTester);
        this.intersectionFinder = Objects.requireNonNull(intersectionFinder);
        this.headshotTester = Objects.requireNonNull(headshotTester);
        this.targetLimiter = Objects.requireNonNull(targetLimiter);
    }

    @Override
    public @NotNull Result findTarget(@NotNull Entity shooter, @NotNull Pos start, @NotNull Point end,
        @NotNull Collection<UUID> previousHits) {
        Instance instance = shooter.getInstance();
        if (instance == null) {
            return new Result(new ArrayList<>(0), new ArrayList<>(0));
        }

        Collection<LivingEntity> nearbyEntities = entityFinder.findEntities(instance, start, end);
        List<Pair<? extends LivingEntity, Vec>> locations = new ArrayList<>(nearbyEntities.size());

        double distanceLimitSquared = start.distanceSquared(end);
        for (LivingEntity entity : nearbyEntities) {
            if (targetTester.useTarget(entity, previousHits)) {
                intersectionFinder.getHitLocation(entity, start, end, distanceLimitSquared).ifPresent(intersection -> {
                    locations.add(Pair.of(entity, intersection));
                });
            }
        }

        List<Pair<? extends LivingEntity, Vec>> adjustedLocations = targetLimiter.limitTargets(start, locations);

        Collection<GunHit> targets = new ArrayList<>(adjustedLocations.size());
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

    /**
     * Data for a {@link BasicTargetFinder}.
     *
     * @param entityFinder       A path to the {@link BasicTargetFinder}'s {@link DirectionalEntityFinder}
     * @param targetTester       A path to the {@link BasicTargetFinder}'s {@link TargetTester}
     * @param intersectionFinder A path to the {@link BasicTargetFinder}'s {@link IntersectionFinder}
     * @param headshotTester     A path to the {@link BasicTargetFinder}'s {@link HeadshotTester}
     * @param targetLimiter      A path to the {@link BasicTargetFinder}'s {@link TargetLimiter}
     */
    @DataObject
    public record Data(
        @NotNull @ChildPath("entity_finder") String entityFinder,
        @NotNull @ChildPath("target_tester") String targetTester,
        @NotNull @ChildPath("intersection_finder") String intersectionFinder,
        @NotNull @ChildPath("headshot_tester") String headshotTester,
        @NotNull @ChildPath("target_limiter") String targetLimiter) {


    }

}
