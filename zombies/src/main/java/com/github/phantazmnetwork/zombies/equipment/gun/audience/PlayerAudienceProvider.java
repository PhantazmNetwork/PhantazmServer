package com.github.phantazmnetwork.zombies.equipment.gun.audience;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link AudienceProvider} which returns a {@link Player}.
 */
public class PlayerAudienceProvider implements AudienceProvider {

    private final PlayerView playerView;

    /**
     * Creates a new {@link PlayerAudienceProvider}.
     *
     * @param playerView The {@link PlayerView} of the {@link Player}
     */
    public PlayerAudienceProvider(@NotNull PlayerView playerView) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        return ConfigProcessor.emptyProcessor(Data::new);
    }

    @Override
    public @NotNull Optional<? extends Player> provideAudience() {
        return playerView.getPlayer();
    }

    /**
     * Data for a {@link PlayerAudienceProvider}.
     */
    public record Data() implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.audience_provider.player");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }
}
