package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An effect which sets the level of a {@link Player} based on the ammo of a gun.
 */
public class AmmoLevelEffect implements GunEffect {

    /**
     * Data for an {@link AmmoLevelEffect}.
     */
    public record Data() implements Keyed {

        /**
         * The serial {@link Key} for this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.level.ammo");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        return ConfigProcessor.emptyProcessor(Data::new);
    }

    private final PlayerView playerView;

    private boolean currentlyActive = false;

    /**
     * Creates a new {@link AmmoLevelEffect}.
     * @param playerView The {@link PlayerView} of the {@link Player} to set the level of
     */
    public AmmoLevelEffect(@NotNull PlayerView playerView) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
    }

    @Override
    public void apply(@NotNull GunState state) {
        if (state.isMainEquipment()) {
            currentlyActive = true;
            playerView.getPlayer().ifPresent(player -> player.setLevel(state.ammo()));
        }
        else if (currentlyActive) {
            playerView.getPlayer().ifPresent(player -> player.setLevel(0));
            currentlyActive = false;
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

}
