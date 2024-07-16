package org.phantazm.zombies.equipment.gun.target;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
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
    private static final Result EMPTY = new Result(List.of());

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
    public BasicTargetFinder(@NotNull @Child("entityFinder") DirectionalEntityFinder entityFinder,
        @NotNull @Child("targetTester") TargetTester targetTester,
        @NotNull @Child("intersectionFinder") IntersectionFinder intersectionFinder,
        @NotNull @Child("headshotTester") HeadshotTester headshotTester,
        @NotNull @Child("targetLimiter") TargetLimiter targetLimiter) {
        this.entityFinder = Objects.requireNonNull(entityFinder);
        this.targetTester = Objects.requireNonNull(targetTester);
        this.intersectionFinder = Objects.requireNonNull(intersectionFinder);
        this.headshotTester = Objects.requireNonNull(headshotTester);
        this.targetLimiter = Objects.requireNonNull(targetLimiter);
    }

    @Override
    public @NotNull Result findTarget(@NotNull Gun gun, @NotNull Entity shooter, @NotNull Pos start, @NotNull Point end,
        @NotNull Collection<UUID> previousHits) {
        Instance instance = shooter.getInstance();
        if (instance == null) {
            return EMPTY;
        }

        double distanceLimitSquared = start.distanceSquared(end);
        List<GunHit> gunHits = new ArrayList<>();
        entityFinder.findEntities(instance, start, end, entity -> {
            if (!targetTester.useTarget(gun, entity, previousHits)) {
                return;
            }

            intersectionFinder.getHitLocation(entity, start, end, distanceLimitSquared).ifPresent(intersection -> {
                gunHits.add(new GunHit(entity, intersection));
            });
        });

        if (gunHits.isEmpty()) {
            return EMPTY;
        }

        List<GunHit> adjustedLocations = targetLimiter.limitTargets(shooter, gun, start, gunHits);
        if (adjustedLocations.isEmpty()) {
            return EMPTY;
        }

        for (GunHit hit : adjustedLocations) {
            if (headshotTester.isHeadshot(shooter, hit.entity(), hit.location())) {
                hit.setHeadshot();
            }

            previousHits.add(hit.entity().getUuid());
        }

        return new Result(adjustedLocations);
    }
}
