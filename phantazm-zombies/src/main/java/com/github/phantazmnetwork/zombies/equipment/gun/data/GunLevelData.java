package com.github.phantazmnetwork.zombies.equipment.gun.data;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunStats;
import com.github.phantazmnetwork.zombies.equipment.gun.effect.GunEffect;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.ReloadTester;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.ShootTester;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.Firer;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.GunStackMapper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Data for a gun level.
 *
 * @param upgrades        Suggested upgrade {@link Key}s for next levels
 * @param stack           The {@link ItemStack} for this level
 * @param stats           A {@link Key} to the gun's {@link GunStats}
 * @param shootTester     A {@link Key} to the gun's {@link ShootTester}
 * @param reloadTester    A {@link Key} to the gun's {@link ReloadTester}
 * @param firer           A {@link Key} to the gun's {@link Firer}
 * @param activateEffects A {@link Collection} of {@link Key}s to the gun's {@link GunEffect}s
 *                        that are invoked when the gun level becomes active
 * @param shootEffects    A {@link Collection} of {@link Key}s to the gun's {@link GunEffect}s
 *                        that are invoked when the gun is shot
 * @param reloadEffects   A {@link Collection} of {@link Key}s to the gun's {@link GunEffect}s
 *                        that are invoked when the gun begins reloading
 * @param tickEffects     A {@link Collection} of {@link Key}s to the gun's {@link GunEffect}s
 *                        that are invoked every tick
 * @param noAmmoEffects   A {@link Collection} of {@link Key}s to the gun's {@link GunEffect}s
 *                        that are invoked when the gun has no ammo
 * @param gunStackMappers A {@link Collection} of {@link Key}s to the gun's {@link GunStackMapper}s
 *                        that produce the visual {@link ItemStack} representation of the gun
 */
public record GunLevelData(@NotNull Set<Key> upgrades,
                           @NotNull ItemStack stack,
                           @NotNull Key stats,
                           @NotNull Key shootTester,
                           @NotNull Key reloadTester,
                           @NotNull Key firer,
                           @NotNull Collection<Key> activateEffects,
                           @NotNull Collection<Key> shootEffects,
                           @NotNull Collection<Key> reloadEffects,
                           @NotNull Collection<Key> tickEffects,
                           @NotNull Collection<Key> noAmmoEffects,
                           @NotNull Collection<Key> gunStackMappers) implements Keyed {

    /**
     * The serial {@link Key} for {@link GunLevelData}.
     */
    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.level");

    /**
     * Creates a {@link GunLevelData}.
     *
     * @param upgrades        Suggested upgrade {@link Key}s for next levels
     * @param stack           The {@link ItemStack} for this level
     * @param stats           A {@link Key} to the gun's {@link GunStats}
     * @param shootTester     A {@link Key} to the gun's {@link ShootTester}
     * @param reloadTester    A {@link Key} to the gun's {@link ReloadTester}
     * @param firer           A {@link Key} to the gun's {@link Firer}
     * @param activateEffects A {@link Collection} of {@link Key}s to the gun's {@link GunEffect}s
     *                        that are invoked when the gun level becomes active
     * @param shootEffects    A {@link Collection} of {@link Key}s to the gun's {@link GunEffect}s
     *                        that are invoked when the gun is shot
     * @param reloadEffects   A {@link Collection} of {@link Key}s to the gun's {@link GunEffect}s
     *                        that are invoked when the gun begins reloading
     * @param tickEffects     A {@link Collection} of {@link Key}s to the gun's {@link GunEffect}s
     *                        that are invoked every tick
     * @param noAmmoEffects   A {@link Collection} of {@link Key}s to the gun's {@link GunEffect}s
     *                        that are invoked when the gun has no ammo
     * @param gunStackMappers A {@link Collection} of {@link Key}s to the gun's {@link GunStackMapper}s
     *                        that produce the visual {@link ItemStack} representation of the gun
     */
    public GunLevelData {
        verifyCollection(upgrades, "upgrades");
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(stats, "stats");
        Objects.requireNonNull(shootTester, "shootTester");
        Objects.requireNonNull(reloadTester, "reloadTester");
        Objects.requireNonNull(firer, "firer");
        verifyCollection(activateEffects, "activateEffects");
        verifyCollection(shootEffects, "shootEffects");
        verifyCollection(reloadEffects, "reloadEffects");
        verifyCollection(tickEffects, "tickEffects");
        verifyCollection(noAmmoEffects, "noAmmoEffects");
        verifyCollection(gunStackMappers, "gunStackMappers");
    }

    /**
     * Creates a dependency consumer for {@link GunLevelData}s.
     *
     * @return A dependency consumer for {@link GunLevelData}s
     */
    public static @NotNull BiConsumer<GunLevelData, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> {
            keys.add(data.stats());
            keys.add(data.shootTester());
            keys.add(data.reloadTester());
            keys.add(data.firer());
            keys.addAll(data.shootEffects());
            keys.addAll(data.reloadEffects());
            keys.addAll(data.tickEffects());
            keys.addAll(data.noAmmoEffects());
            keys.addAll(data.gunStackMappers());
        };
    }

    private void verifyCollection(@NotNull Collection<?> collection, @NotNull String name) {
        Objects.requireNonNull(collection, name);
        for (Object element : collection) {
            Objects.requireNonNull(element, name + " element");
        }
    }

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
