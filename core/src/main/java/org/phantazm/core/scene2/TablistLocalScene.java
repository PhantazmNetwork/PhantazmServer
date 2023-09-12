package org.phantazm.core.scene2;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.CachedPacket;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;

import java.util.Optional;
import java.util.Set;

/**
 * A {@link Scene} that sends tablist packets to remaining players after others are removed from this scene.
 */
public interface TablistLocalScene extends Scene {
    default void postLeave(@NotNull Set<@NotNull PlayerView> leftPlayers) {
        if (leftPlayers.isEmpty()) {
            return;
        }

        for (PlayerView left : leftPlayers) {
            Optional<Player> leftOptional = left.getPlayer();
            if (leftOptional.isEmpty()) {
                continue;
            }

            Player leftPlayer = leftOptional.get();
            CachedPacket removalPacket = new CachedPacket(leftPlayer::getRemovePlayerToList);

            for (PlayerView existingPlayer : playersView()) {
                Optional<Player> existingOptional = existingPlayer.getPlayer();
                if (existingOptional.isEmpty()) {
                    continue;
                }

                Player existing = existingOptional.get();
                if (!existing.isViewer(leftPlayer)) {
                    existing.sendPacket(removalPacket);
                }
            }
        }
    }
}
