package com.github.phantazmnetwork.zombies.equipment.gun;

import com.github.phantazmnetwork.zombies.equipment.upgrade.UpgradeNode;
import com.github.phantazmnetwork.zombies.equipment.gun.effect.GunEffect;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.ReloadTester;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.ShootTester;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.Firer;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.GunStackMapper;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * An individual gun level of a gun.
 *
 * @param upgrades        Suggested upgrade {@link Key}s for next levels
 * @param stack           The {@link ItemStack} for this level
 * @param stats           The gun's {@link GunStats}
 * @param shootTester     The gun's {@link ShootTester}
 * @param reloadTester    The gun's {@link ReloadTester}
 * @param firer           The gun's {@link Firer}
 * @param activateEffects The gun's {@link GunEffect}s that are invoked when the gun level becomes active
 * @param shootEffects    The gun's {@link GunEffect}s that are invoked when the gun is shot
 * @param reloadEffects   The gun's {@link GunEffect}s that are invoked when the gun begins reloading
 * @param tickEffects     The gun's {@link GunEffect}s that are invoked every tick
 * @param noAmmoEffects   The gun's {@link GunEffect}s that are invoked when the gun has no ammo
 * @param gunStackMappers The gun's {@link GunStackMapper}s that produce the visual {@link ItemStack} representation of the gun
 */
public record GunLevel(@NotNull Set<Key> upgrades,
                       @NotNull ItemStack stack,
                       @NotNull GunStats stats,
                       @NotNull ShootTester shootTester,
                       @NotNull ReloadTester reloadTester,
                       @NotNull Firer firer,
                       @NotNull Collection<GunEffect> activateEffects,
                       @NotNull Collection<GunEffect> shootEffects,
                       @NotNull Collection<GunEffect> reloadEffects,
                       @NotNull Collection<GunEffect> tickEffects,
                       @NotNull Collection<GunEffect> noAmmoEffects,
                       @NotNull Collection<GunStackMapper> gunStackMappers) implements UpgradeNode {

    /**
     * Creates a {@link GunLevel}.
     *
     * @param upgrades        Suggested upgrade {@link Key}s for next levels
     * @param stack           The {@link ItemStack} for this level
     * @param stats           The gun's {@link GunStats}
     * @param shootTester     The gun's {@link ShootTester}
     * @param reloadTester    The gun's {@link ReloadTester}
     * @param firer           The gun's {@link Firer}
     * @param activateEffects The gun's {@link GunEffect}s that are invoked when the gun level becomes active
     * @param shootEffects    The gun's {@link GunEffect}s that are invoked when the gun is shot
     * @param reloadEffects   The gun's {@link GunEffect}s that are invoked when the gun begins reloading
     * @param tickEffects     The gun's {@link GunEffect}s that are invoked every tick
     * @param noAmmoEffects   The gun's {@link GunEffect}s that are invoked when the gun has no ammo
     * @param gunStackMappers The gun's {@link GunStackMapper}s that produce the visual {@link ItemStack} representation of the gun
     */
    public GunLevel {
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

    private void verifyCollection(@NotNull Collection<?> collection, @NotNull String name) {
        Objects.requireNonNull(collection, name);
        for (Object element : collection) {
            Objects.requireNonNull(element, name + " element");
        }
    }

}
