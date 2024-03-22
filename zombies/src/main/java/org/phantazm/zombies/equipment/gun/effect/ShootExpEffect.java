package org.phantazm.zombies.equipment.gun.effect;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.MathUtils;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.GunStats;
import org.phantazm.zombies.equipment.gun.GunUtils;

import java.util.Objects;

/**
 * A {@link GunEffect} that sets a {@link Player}'s exp based on the time since their last shot.
 */
@Model("zombies.gun.effect.exp.shoot")
@Cache(false)
public class ShootExpEffect implements GunEffect {

    private final PlayerView playerView;

    private final GunStats stats;

    private boolean currentlyActive = false;

    /**
     * Creates a {@link ShootExpEffect}.
     *
     * @param playerView The {@link PlayerView} of the {@link Player} to set the exp of
     * @param stats      The {@link GunStats} of the gun
     */
    @FactoryMethod
    public ShootExpEffect(@NotNull PlayerView playerView, @NotNull @Child("stats") GunStats stats) {
        this.playerView = Objects.requireNonNull(playerView);
        this.stats = Objects.requireNonNull(stats);
    }

    @Override
    public void apply(@NotNull GunState state) {
        if (state.isMainEquipment()) {
            float exp = MathUtils.clamp(
                state.ammo() > 0 ? (state.ticksSinceLastShot() * fireRateFactor()) / (float) stats.shootSpeed() : 0F,
                0, 1);

            playerView.getPlayer().ifPresent(player -> player.setExp(exp));
            currentlyActive = true;
        } else if (currentlyActive) {
            playerView.getPlayer().ifPresent(player -> player.setExp(0));
            currentlyActive = false;
        }
    }

    private float fireRateFactor() {
        return playerView.getPlayer().map(GunUtils::fireRateFactor).orElse(1F);
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }
}
