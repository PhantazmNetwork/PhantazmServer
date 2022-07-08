package com.github.phantazmnetwork.zombies.equipment.gun;

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

public record GunLevel(@NotNull Set<Key> upgrades,
                       @NotNull ItemStack stack,
                       @NotNull GunStats stats,
                       @NotNull ShootTester shootTester,
                       @NotNull ReloadTester reloadTester,
                       @NotNull Firer firer,
                       @NotNull Collection<GunEffect> startEffects,
                       @NotNull Collection<GunEffect> shootEffects,
                       @NotNull Collection<GunEffect> reloadEffects,
                       @NotNull Collection<GunEffect> tickEffects,
                       @NotNull Collection<GunEffect> noAmmoEffects,
                       @NotNull Collection<GunStackMapper> gunStackMappers) {

    public GunLevel {
        verifyCollection(upgrades, "upgrades");
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(stats, "stats");
        Objects.requireNonNull(shootTester, "shootTester");
        Objects.requireNonNull(reloadTester, "reloadTester");
        Objects.requireNonNull(firer, "firer");
        verifyCollection(startEffects, "startEffects");
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
