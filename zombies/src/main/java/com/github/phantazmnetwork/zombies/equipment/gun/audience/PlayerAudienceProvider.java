package com.github.phantazmnetwork.zombies.equipment.gun.audience;

import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link AudienceProvider} which returns a {@link Player}.
 */
@Model("zombies.gun.audience_provider.player")
public class PlayerAudienceProvider implements AudienceProvider {

    private final PlayerView playerView;

    /**
     * Creates a new {@link PlayerAudienceProvider}.
     *
     * @param playerView The {@link PlayerView} of the {@link Player}
     */
    @FactoryMethod
    public PlayerAudienceProvider(@NotNull @Dependency("zombies.dependency.gun.player_view") PlayerView playerView) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
    }

    @Override
    public @NotNull Optional<? extends Player> provideAudience() {
        return playerView.getPlayer();
    }

}
