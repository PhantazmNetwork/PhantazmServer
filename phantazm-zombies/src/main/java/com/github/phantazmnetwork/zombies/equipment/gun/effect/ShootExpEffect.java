package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import net.kyori.adventure.key.Keyed;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunStats;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ShootExpEffect implements GunEffect {

    public record Data(@NotNull Key gunStatsKey) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.exp.shoot");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    private boolean currentlyActive = false;

    private final Data data;

    private final PlayerView playerView;

    private final GunStats stats;

    public ShootExpEffect(@NotNull Data data, @NotNull PlayerView playerView, @NotNull GunStats stats) {
        this.data = Objects.requireNonNull(data, "data");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.stats = Objects.requireNonNull(stats, "stats");
    }

    @Override
    public void accept(@NotNull GunState state) {
        if (state.isMainEquipment()) {
            float exp = state.ammo() > 0 ? (float) state.ticksSinceLastShot() / stats.shootSpeed() : 0F;
            playerView.getPlayer().ifPresent(player -> player.setExp(exp));
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

    @Override
    public @NotNull Keyed getData() {
        return data;
    }

}
