package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AmmoLevelEffect implements GunEffect {

    public record Data() implements VariantSerializable {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.level.ammo");

        @Override
        public @NotNull Key getSerialKey() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    private final PlayerView playerView;

    private boolean currentlyActive = false;

    public AmmoLevelEffect(@NotNull Data data, @NotNull PlayerView playerView) {
        this.data = data;
        this.playerView = Objects.requireNonNull(playerView, "playerView");
    }

    @Override
    public void accept(@NotNull GunState state) {
        if (state.isMainEquipment()) {
            currentlyActive = true;
            playerView.getPlayer().ifPresent(player -> {
                player.setLevel(state.ammo());
            });
        }
        else if (currentlyActive) {
            playerView.getPlayer().ifPresent(player -> {
                player.setLevel(0);
            });
            currentlyActive = false;
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @Override
    public @NotNull VariantSerializable getData() {
        return data;
    }

}
