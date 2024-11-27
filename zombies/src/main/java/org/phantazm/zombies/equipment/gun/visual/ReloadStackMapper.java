package org.phantazm.zombies.equipment.gun.visual;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
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
    private final PlayerView player;

    /**
     * Creates a {@link ReloadStackMapper}.
     *
     * @param stats        The gun's {@link GunStats}
     * @param reloadTester The gun's {@link ReloadTester}
     */
    @FactoryMethod
    public ReloadStackMapper(@NotNull @Child("stats") GunStats stats,
        @NotNull @Child("reloadTester") ReloadTester reloadTester, @NotNull PlayerView player) {
        this.stats = Objects.requireNonNull(stats);
        this.reloadTester = Objects.requireNonNull(reloadTester);
        this.player = player;
    }

    @Override
    public @NotNull ItemStack map(@NotNull GunState state, @NotNull ItemStack intermediate) {
        if (reloadTester.isReloading(state)) {
            Player actualPlayer = player.getPlayer().orElse(null);
            long reloadSpeed = stats.reloadSpeed(actualPlayer);
            int maxDamage = intermediate.material().registry().maxDamage();
            int damage = maxDamage - (int) (maxDamage * ((double) state.ticksSinceLastReload() / reloadSpeed));

            return intermediate.withMeta(builder -> builder.damage(damage));
        }

        return intermediate;
    }
}
