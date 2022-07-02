package com.github.phantazmnetwork.zombies.equipment.gun.target;

import com.github.phantazmnetwork.api.RayUtils;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.api.target.TargetSelectorInstance;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.shot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.target.TargetSelector;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class LinearTargetSelector implements TargetSelector<GunShot> {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "selector.gun.linear");

    private record BaseGunShot(@NotNull Point end,
                               @NotNull Collection<Pair<PhantazmMob, Vec>> regularTargets,
                               @NotNull Collection<Pair<PhantazmMob, Vec>> headshotTargets) implements GunShot {

        @Override
        public @NotNull Point getEnd() {
            return end;
        }

        @Override
        public @NotNull Collection<Pair<PhantazmMob, Vec>> getRegularTargets() {
            return regularTargets;
        }

        @Override
        public @NotNull Collection<Pair<PhantazmMob, Vec>> getHeadshotTargets() {
            return headshotTargets;
        }
    }

    private final TargetSelector<Point> shotEndSelector;

    public LinearTargetSelector(@NotNull TargetSelector<Point> shotEndSelector) {
        this.shotEndSelector = Objects.requireNonNull(shotEndSelector);
    }

    @Override
    public @NotNull TargetSelectorInstance<GunShot> createSelector(@NotNull MobStore store, @NotNull PlayerView playerView) {
        TargetSelectorInstance<Point> shotEndSelectorInstance = shotEndSelector.createSelector(store, playerView);

        return () -> playerView.getPlayer().map(player -> {
            Instance instance = player.getInstance();
            if (instance == null) {
                return null;
            }

            Pos start = player.getPosition().add(0, player.getEyeHeight(), 0);

            Optional<Point> endOptional = shotEndSelectorInstance.selectTarget();
            if (endOptional.isEmpty()) {
                return null;
            }
            Point end = endOptional.get();
            double range = end.distance(start);

            Collection<Entity> nearbyEntities = instance.getNearbyEntities(start, range);
            Collection<Pair<PhantazmMob, Vec>> targets = new ArrayList<>(nearbyEntities.size());
            Collection<Pair<PhantazmMob, Vec>> headshots = new ArrayList<>(nearbyEntities.size());
            for (Entity entity : nearbyEntities) {
                PhantazmMob mob = store.getMob(entity.getUuid());
                if (mob != null) {
                    RayUtils.rayTrace(entity.getBoundingBox(), entity.getPosition(), start).ifPresent(intersection -> {
                        if (intersection.y() < entity.getPosition().y() + entity.getEyeHeight()) {
                            targets.add(Pair.of(mob, intersection));
                        } else {
                            headshots.add(Pair.of(mob, intersection));
                        }
                    });
                }
            }

            return new BaseGunShot(end, targets, headshots);
        });
    }

    @Override
    public @NotNull Key getSerialKey() {
        return SERIAL_KEY;
    }
}
