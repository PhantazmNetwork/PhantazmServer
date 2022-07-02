package com.github.phantazmnetwork.zombies.equipment.gun;

import com.github.phantazmnetwork.zombies.equipment.gun.effect.GunEffect;
import com.github.phantazmnetwork.zombies.equipment.gun.shot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.gun.shot.handler.ShotHandler;
import com.github.phantazmnetwork.zombies.equipment.gun.visual.GunStackMapper;
import com.github.phantazmnetwork.zombies.equipment.target.TargetSelector;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record GunLevel(@NotNull ItemStack stack,
                       @NotNull TargetSelector<? extends GunShot> selector,
                       @NotNull Collection<GunEffect> reloadEffects,
                       @NotNull Collection<GunEffect> tickEffects,
                       @NotNull Collection<GunEffect> emptyClipEffects,
                       @NotNull Collection<ShotHandler> shotHandlers,
                       @NotNull Collection<GunStackMapper> gunStackMappers,
                       long shootSpeed,
                       long reloadSpeed,
                       int maxAmmo,
                       int maxClip,
                       int shots) {
}
