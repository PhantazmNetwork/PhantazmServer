package com.github.phantazmnetwork.zombies.equipment.gun;

import com.github.phantazmnetwork.zombies.equipment.gun.effect.GunEffect;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.ReloadTester;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.ShootTester;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.Firer;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.GunStackMapper;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public record GunLevel(@NotNull ItemStack stack,
                       @NotNull GunStats stats,
                       @NotNull ShootTester shootTester,
                       @NotNull ReloadTester reloadTester,
                       @NotNull Firer firer,
                       @NotNull Collection<GunEffect> reloadEffects,
                       @NotNull Collection<GunEffect> tickEffects,
                       @NotNull Collection<GunEffect> emptyClipEffects,
                       @NotNull Collection<GunStackMapper> gunStackMappers) {

    public GunLevel {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(stats, "stats");
        Objects.requireNonNull(shootTester, "shootTester");
        Objects.requireNonNull(reloadTester, "reloadTester");
        Objects.requireNonNull(firer, "firer");
        Objects.requireNonNull(reloadEffects, "reloadEffects");
        Objects.requireNonNull(tickEffects, "tickEffects");
        Objects.requireNonNull(emptyClipEffects, "emptyClipEffects");
        Objects.requireNonNull(gunStackMappers, "gunStackMappers");
    }

}
