package org.phantazm.zombies.equipment.gun.visual;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.GunStats;
import org.phantazm.zombies.equipment.gun.reload.ReloadTester;

import java.util.Objects;

/**
 * A {@link GunStackMapper} that maps based on a gun's reload progress.
 */
@Model("zombies.gun.stack_mapper.reload.durability")
@Cache(false)
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
    public ReloadStackMapper(@NotNull @Child("stats") GunStats stats,
        @NotNull @Child("reload_tester") ReloadTester reloadTester) {
        this.stats = Objects.requireNonNull(stats);
        this.reloadTester = Objects.requireNonNull(reloadTester);
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

    /**
     * Data for a {@link ReloadStackMapper}.
     *
     * @param stats        A path to the gun's {@link GunStats}
     * @param reloadTester A path to the gun's {@link ReloadTester}
     */
    @DataObject
    public record Data(
        @NotNull @ChildPath("stats") String stats,
        @NotNull @ChildPath("reload_tester") String reloadTester) {
    }
}
