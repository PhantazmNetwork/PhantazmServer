package org.phantazm.zombies.equipment.gun.audience;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link AudienceProvider} which returns a {@link Player}.
 */
@Model("zombies.gun.audience_provider.player")
@Cache(false)
public class PlayerAudienceProvider implements AudienceProvider {

    private final PlayerView playerView;

    /**
     * Creates a new {@link PlayerAudienceProvider}.
     *
     * @param playerView The {@link PlayerView} of the {@link Player}
     */
    @FactoryMethod
    public PlayerAudienceProvider(@NotNull PlayerView playerView) {
        this.playerView = Objects.requireNonNull(playerView);
    }

    @Override
    public @NotNull Optional<? extends Player> provideAudience() {
        return playerView.getPlayer();
    }

}
