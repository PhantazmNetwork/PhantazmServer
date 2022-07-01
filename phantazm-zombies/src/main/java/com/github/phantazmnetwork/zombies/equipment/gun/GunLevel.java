package com.github.phantazmnetwork.zombies.equipment.gun;

import com.github.phantazmnetwork.zombies.equipment.gun.visual.GunStackMapper;
import com.github.phantazmnetwork.zombies.equipment.target.TargetSelector;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Consumer;

public record GunLevel(@NotNull ItemStack stack,
                       @NotNull TargetSelector<? extends GunHit> selector,
                       @NotNull Collection<Consumer<Gun>> shootEffects,
                       @NotNull Collection<Consumer<Gun>> reloadEffects,
                       @NotNull Collection<Consumer<Gun>> tickEffects,
                       @NotNull Collection<Consumer<Gun>> emptyClipEffects,
                       @NotNull Collection<GunStackMapper> gunStackMappers,
                       long shootSpeed,
                       long reloadSpeed,
                       int maxAmmo,
                       int maxClip) {
}
