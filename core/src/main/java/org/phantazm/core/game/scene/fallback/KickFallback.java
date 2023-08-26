package org.phantazm.core.game.scene.fallback;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link SceneFallback} which kicks {@link Player}s.
 */
public class KickFallback implements SceneFallback {

    private final Component kickMessage;

    /**
     * Creates a kick fallback.
     *
     * @param kickMessage The message used for kicks
     */
    public KickFallback(@NotNull Component kickMessage) {
        this.kickMessage = Objects.requireNonNull(kickMessage);
    }

    @Override
    public CompletableFuture<Boolean> fallback(@NotNull PlayerView playerView) {
        Optional<Player> playerOptional = playerView.getPlayer();
        if (playerOptional.isPresent()) {
            playerOptional.get().kick(kickMessage);
            return CompletableFuture.completedFuture(true);
        }

        return CompletableFuture.completedFuture(false);
    }

}
