package com.github.phantazmnetwork.zombies.equipment.gun.data;

import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

public record GunLevelData(@NotNull Set<Key> upgrades,
                           @NotNull ItemStack stack,
                           @NotNull Key stats,
                           @NotNull Key shootTester,
                           @NotNull Key reloadTester,
                           @NotNull Key firer,
                           @NotNull Collection<Key> startEffects,
                           @NotNull Collection<Key> shootEffects,
                           @NotNull Collection<Key> reloadEffects,
                           @NotNull Collection<Key> tickEffects,
                           @NotNull Collection<Key> noAmmoEffects,
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
            keys.addAll(data.noAmmoEffects());
            keys.addAll(data.gunStackMappers());
        };
    }

    public GunLevelData {
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

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
