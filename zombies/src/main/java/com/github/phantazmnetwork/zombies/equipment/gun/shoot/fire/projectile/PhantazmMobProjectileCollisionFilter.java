package com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.projectile;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link ProjectileCollisionFilter} which explodes when it comes in contact with {@link PhantazmMob}s.
 */
public class PhantazmMobProjectileCollisionFilter implements ProjectileCollisionFilter {

    private final MobStore mobStore;

    /**
     * Creates a new {@link PhantazmMobProjectileCollisionFilter}.
     *
     * @param mobStore A {@link MobStore} to retrive {@link PhantazmMob}s from
     */
    public PhantazmMobProjectileCollisionFilter(@NotNull MobStore mobStore) {
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        return ConfigProcessor.emptyProcessor(Data::new);
    }

    @Override
    public boolean shouldExplode(@NotNull Entity cause) {
        return mobStore.getMob(cause.getUuid()) != null;
    }

    /**
     * Data for a {@link PhantazmMobProjectileCollisionFilter}.
     */
    public record Data() implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY =
                Key.key(Namespaces.PHANTAZM, "gun.firer.projectile.collision_filter.phantazm");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

}
