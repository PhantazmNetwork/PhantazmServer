package com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.projectile;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PhantazmProjectileCollisionFilter implements ProjectileCollisionFilter {

    public record Data() implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM,
                "gun.firer.projectile.collision_filter.phantazm");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                return new LinkedConfigNode(0);
            }
        };
    }

    private final MobStore mobStore;

    public PhantazmProjectileCollisionFilter(@NotNull MobStore mobStore) {
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
    }

    @Override
    public boolean shouldExplode(@NotNull Entity cause) {
        return mobStore.getMob(cause.getUuid()) != null;
    }

}
