package org.phantazm.zombies.equipment.gun2.target;

import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.equipment.gun2.shoot.GunHit;
import org.phantazm.zombies.equipment.gun2.target.entityfinder.directional.DirectionalEntityFinder;
import org.phantazm.zombies.equipment.gun2.target.headshot.HeadshotTester;
import org.phantazm.zombies.equipment.gun2.target.intersectionfinder.IntersectionFinder;
import org.phantazm.zombies.equipment.gun2.target.limiter.TargetLimiter;
import org.phantazm.zombies.equipment.gun2.target.tester.TargetTester;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;

public class BasicTargetFinder implements PlayerComponent<TargetFinder> {

    private final PlayerComponent<DirectionalEntityFinder> entityFinder;

    private final PlayerComponent<TargetTester> targetTester;

    private final PlayerComponent<IntersectionFinder> intersectionFinder;

    private final PlayerComponent<TargetLimiter> targetLimiter;

    private final PlayerComponent<HeadshotTester> headshotTester;

    public BasicTargetFinder(@NotNull PlayerComponent<DirectionalEntityFinder> entityFinder, @NotNull PlayerComponent<TargetTester> targetTester, @NotNull PlayerComponent<IntersectionFinder> intersectionFinder, @NotNull PlayerComponent<TargetLimiter> targetLimiter, @NotNull PlayerComponent<HeadshotTester> headshotTester) {
        this.entityFinder = Objects.requireNonNull(entityFinder);
        this.targetTester = Objects.requireNonNull(targetTester);
        this.intersectionFinder = Objects.requireNonNull(intersectionFinder);
        this.targetLimiter = Objects.requireNonNull(targetLimiter);
        this.headshotTester = Objects.requireNonNull(headshotTester);
    }

    @Override
    public @NotNull TargetFinder forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        return new Finder(entityFinder.forPlayer(player, injectionStore), targetTester.forPlayer(player, injectionStore), intersectionFinder.forPlayer(player, injectionStore), targetLimiter.forPlayer(player, injectionStore), headshotTester.forPlayer(player, injectionStore));
    }

    private static class Finder implements TargetFinder {

        private final DirectionalEntityFinder entityFinder;

        private final TargetTester targetTester;

        private final IntersectionFinder intersectionFinder;

        private final TargetLimiter targetLimiter;

        private final HeadshotTester headshotTester;

        public Finder(DirectionalEntityFinder entityFinder, TargetTester targetTester, IntersectionFinder intersectionFinder, TargetLimiter targetLimiter, HeadshotTester headshotTester) {
            this.entityFinder = entityFinder;
            this.targetTester = targetTester;
            this.intersectionFinder = intersectionFinder;
            this.targetLimiter = targetLimiter;
            this.headshotTester = headshotTester;
        }

        @Override
        public @NotNull Result findTarget(@NotNull Entity shooter, @NotNull Pos start, @NotNull Point end, @NotNull Collection<UUID> previousHits) {
            Instance instance = shooter.getInstance();
            if (instance == null) {
                return new TargetFinder.Result(new ArrayList<>(0), new ArrayList<>(0));
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

            return new TargetFinder.Result(targets, headshots);
        }
    }

}
