package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AmmoLevelEffect implements GunEffect {

    public record Data() implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.level.ammo");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        return ConfigProcessor.emptyProcessor(Data::new);
    }

    private final PlayerView playerView;

    private boolean currentlyActive = false;

    public AmmoLevelEffect(@NotNull PlayerView playerView) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
    }

    @Override
    public void apply(@NotNull GunState state) {
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

}
