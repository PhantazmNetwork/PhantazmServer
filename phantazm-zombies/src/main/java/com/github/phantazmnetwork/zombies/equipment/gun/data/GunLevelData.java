package com.github.phantazmnetwork.zombies.equipment.gun.data;

import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;

public record GunLevelData(int order, // TODO: key based upgrades
                           @NotNull ItemStack stack,
                           @NotNull Key stats,
                           @NotNull Key shootTester,
                           @NotNull Key reloadTester,
                           @NotNull Key firer,
                           @NotNull Collection<Key> shootEffects,
                           @NotNull Collection<Key> reloadEffects,
                           @NotNull Collection<Key> tickEffects,
                           @NotNull Collection<Key> emptyClipEffects,
                           @NotNull Collection<Key> gunStackMappers) implements Keyed {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.level");

    public static @NotNull BiConsumer<GunLevelData, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> {
            keys.add(data.stats());
            keys.add(data.shootTester());
            keys.add(data.reloadTester());
            keys.add(data.firer());
            keys.addAll(data.shootEffects());
            keys.addAll(data.reloadEffects());
            keys.addAll(data.tickEffects());
            keys.addAll(data.emptyClipEffects());
            keys.addAll(data.gunStackMappers());
        };
    }

    public GunLevelData {
        if (order() < 0) {
            throw new IllegalArgumentException("order must be greater than or equal to 0");
        }
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
