package com.github.phantazmnetwork.api.game.scene.fallback;

import com.github.phantazmnetwork.api.player.PlayerView;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A {@link SceneFallback} which kicks {@link Player}s.
 */
@SuppressWarnings("ClassCanBeRecord")
public class KickFallback implements SceneFallback {

    private final Component kickMessage;

    /**
     * Creates a kick fallback.
     * @param kickMessage The message used for kicks
     */
    public KickFallback(@NotNull Component kickMessage) {
        this.kickMessage = kickMessage;
    }

    @Override
    public boolean fallback(@NotNull PlayerView playerView) {
        Optional<Player> playerOptional = playerView.getPlayer();
        if (playerOptional.isPresent()) {
            playerOptional.get().kick(kickMessage);
            return true;
        }

        return false;
    }

}
