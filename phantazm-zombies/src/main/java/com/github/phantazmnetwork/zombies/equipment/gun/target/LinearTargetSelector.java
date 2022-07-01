package com.github.phantazmnetwork.zombies.equipment.gun.target;

import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.api.target.TargetSelectorInstance;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.GunHit;
import com.github.phantazmnetwork.zombies.equipment.target.TargetSelector;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class LinearTargetSelector implements TargetSelector<GunHit> {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "selector.gun.linear");

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull TargetSelectorInstance<GunHit> createSelector(@NotNull MobStore store, @NotNull PlayerView playerView) {
        return () -> playerView.getPlayer().map(player -> {
            Pos position = player.getPosition();
            Vec start = position.withY(position.y() + player.getEyeHeight()).asVec();
            Point end = player.getTargetBlockPosition(100);

            Instance instance = player.getInstance();
            if (instance == null) {
                return null;
            }

            Collection<Entity> nearbyEntities = instance.getNearbyEntities(start, 100);
            Collection<PhantazmMob> targets = new ArrayList<>(nearbyEntities.size()); // TODO: scale?
            for (Entity entity : nearbyEntities) {
                if (entity.getBoundingBox().boundingBoxRayIntersectionCheck(start, player.getPosition().direction(),
                        entity.getPosition())) {
                    PhantazmMob mob = store.getMob(entity.getUuid());
                    if (mob != null) {
                        targets.add(mob);
                    }
                }
            }

            return new GunHit() {
                @Override
                public @NotNull Point getEnd() {
                    return end;
                }

                @Override
                public @NotNull Collection<PhantazmMob> getHeadshotTargets() {
                    return Collections.emptyList();
                }

                @Override
                public @NotNull Collection<PhantazmMob> getRegularTargets() {
                    return targets;
                }
            };
        });
    }

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
