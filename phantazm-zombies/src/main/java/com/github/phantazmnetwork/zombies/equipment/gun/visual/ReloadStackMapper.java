package com.github.phantazmnetwork.zombies.equipment.gun.visual;

import net.kyori.adventure.key.Keyed;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.GunStats;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.ReloadTester;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ReloadStackMapper implements GunStackMapper {

    public record Data(@NotNull Key statsKey, @NotNull Key reloadTesterKey) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.stack_mapper.reload.durability");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    private final GunStats stats;

    private final ReloadTester reloadTester;

    public ReloadStackMapper(@NotNull Data data, @NotNull GunStats stats, @NotNull ReloadTester reloadTester) {
        this.data = Objects.requireNonNull(data, "data");
        this.stats = Objects.requireNonNull(stats, "stats");
        this.reloadTester = Objects.requireNonNull(reloadTester, "reloadTester");
    }

    @Override
    public @NotNull ItemStack map(@NotNull GunState state, @NotNull ItemStack intermediate) {
        if (reloadTester.isReloading(state)) {
            long reloadSpeed = stats.reloadSpeed();
            int maxDamage = intermediate.material().registry().maxDamage();
            int damage = maxDamage - (int) (maxDamage * ((double) state.ticksSinceLastReload() / reloadSpeed));

            return intermediate.withMeta(builder -> builder.damage(damage));
        }

        return intermediate;
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }

}
