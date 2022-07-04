package com.github.phantazmnetwork.zombies.equipment.gun.target;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional.DirectionalEntityFinder;
import com.github.phantazmnetwork.zombies.equipment.gun.target.headshot.HeadshotTester;
import com.github.phantazmnetwork.zombies.equipment.gun.target.tester.TargetTester;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class BasicTargetFinder implements TargetFinder {

    public record Data(@NotNull Key finderKey, @NotNull Key targetTesterKey, @NotNull Key headshotTesterKey,
                       boolean ignorePreviousHits)
            implements VariantSerializable {

        public static final Key SERIAL_KEY = Key.key("gun.target_finder.basic");

        @Override
        public @NotNull Key getSerialKey() {
            return SERIAL_KEY;
        }
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
        Collection<GunHit> targets = new ArrayList<>(nearbyEntities.size());
        Collection<GunHit> headshots = new ArrayList<>(nearbyEntities.size());
        for (Entity entity : nearbyEntities) {
            PhantazmMob mob = store.getMob(entity.getUuid());
            if (mob != null && !(data.ignorePreviousHits() && previousHits.contains(mob))) {
                targetTester.getHitLocation(player, mob, start).ifPresent(intersection -> {
                    if (headshotTester.isHeadshot(player, mob, intersection)) {
                        headshots.add(new GunHit(mob, intersection));
                    } else {
                        targets.add(new GunHit(mob, intersection));
                    }
                    previousHits.add(mob);
                });
            }
        }

        return new Result(targets, headshots);
    }

    @Override
    public @NotNull VariantSerializable getData() {
        return data;
    }

}
