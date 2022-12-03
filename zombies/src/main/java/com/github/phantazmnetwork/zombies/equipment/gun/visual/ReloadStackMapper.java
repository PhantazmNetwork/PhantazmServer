package com.github.phantazmnetwork.zombies.equipment.gun.visual;

import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.GunStats;
import com.github.phantazmnetwork.zombies.equipment.gun.reload.ReloadTester;
import com.github.steanky.element.core.annotation.*;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link GunStackMapper} that maps based on a gun's reload progress.
 */
@Model("zombies.gun.stack_mapper.reload.durability")
public class ReloadStackMapper implements GunStackMapper {

    private final GunStats stats;
    private final ReloadTester reloadTester;

    /**
     * Creates a {@link ReloadStackMapper}.
     *
     * @param stats        The gun's {@link GunStats}
     * @param reloadTester The gun's {@link ReloadTester}
     */
    @FactoryMethod
    public ReloadStackMapper(@NotNull Data data, @NotNull @DataName("stats") GunStats stats,
            @NotNull @DataName("reload_tester") ReloadTester reloadTester) {
        this.stats = Objects.requireNonNull(stats, "stats");
        this.reloadTester = Objects.requireNonNull(reloadTester, "reloadTester");
    }

    @Override
    public @NotNull ItemStack map(@NotNull GunState state, @NotNull ItemStack intermediate) {
        if (reloadTester.isReloading(state)) {
            long reloadSpeed = stats.reloadSpeed();
            int maxDamage = intermediate.material().registry().maxDamage();
            int damage = maxDamage - (int)(maxDamage * ((double)state.ticksSinceLastReload() / reloadSpeed));

            return intermediate.withMeta(builder -> builder.damage(damage));
        }

        return intermediate;
    }

    /**
     * Data for a {@link ReloadStackMapper}.
     *
     * @param statsPath        A path to the gun's {@link GunStats}
     * @param reloadTesterPath A path to the gun's {@link ReloadTester}
     */
    @DataObject
    public record Data(@NotNull @DataPath("stats") String statsPath,
                       @NotNull @DataPath("reload_tester") String reloadTesterPath) {

        /**
         * Creates a {@link Data}.
         *
         * @param statsPath        A path to the gun's {@link GunStats}
         * @param reloadTesterPath A path to the gun's {@link ReloadTester}
         */
        public Data {
            Objects.requireNonNull(statsPath, "statsPath");
            Objects.requireNonNull(reloadTesterPath, "reloadTesterPath");
        }

    }

}
