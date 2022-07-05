package com.github.phantazmnetwork.zombies.equipment.gun;

import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public record GunLevelData(@NotNull ItemStack stack,
                           @NotNull GunStats stats,
                           @NotNull Keyed shootTester,
                           @NotNull Keyed reloadTester,
                           @NotNull Keyed firer,
                           @NotNull Collection<Keyed> shootEffects,
                           @NotNull Collection<Keyed> reloadEffects,
                           @NotNull Collection<Keyed> tickEffects,
                           @NotNull Collection<Keyed> emptyClipEffects,
                           @NotNull Collection<Keyed> gunStackMappers) implements Keyed {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.level");

    public GunLevelData {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(stats, "stats");
        Objects.requireNonNull(shootTester, "shootTester");
        Objects.requireNonNull(reloadTester, "reloadTester");
        Objects.requireNonNull(firer, "firer");
        Objects.requireNonNull(shootEffects, "shootEffects");
        Objects.requireNonNull(reloadEffects, "reloadEffects");
        Objects.requireNonNull(tickEffects, "tickEffects");
        Objects.requireNonNull(emptyClipEffects, "emptyClipEffects");
        Objects.requireNonNull(gunStackMappers, "gunStackMappers");
    }

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
