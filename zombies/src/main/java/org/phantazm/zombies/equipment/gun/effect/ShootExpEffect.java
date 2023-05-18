package org.phantazm.zombies.equipment.gun.effect;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.GunStats;

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
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.stats = Objects.requireNonNull(stats, "stats");
    }

    @Override
    public void apply(@NotNull GunState state) {
        if (state.isMainEquipment()) {
            float exp = state.ammo() > 0
                        ? (float)state.ticksSinceLastShot() / stats.shootSpeed()
                        : 0F; // TODO: fix for fire speed

            if (exp >= 0 && exp <= 1) {
                playerView.getPlayer().ifPresent(player -> player.setExp(exp));
            }

            currentlyActive = true;
        }
        else if (currentlyActive) {
            playerView.getPlayer().ifPresent(player -> player.setExp(0));
            currentlyActive = false;
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    /**
     * Data for a {@link ShootExpEffect}.
     *
     * @param stats A path to the gun's {@link GunStats}
     */
    @DataObject
    public record Data(@NotNull @ChildPath("stats") String stats) {
    }
}
