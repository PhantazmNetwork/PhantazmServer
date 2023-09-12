package org.phantazm.core.scene2;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.CachedPacket;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A {@link Scene} that sends tablist removal packets to remaining players after others are removed from this scene.
 */
public interface TablistLocalScene extends Scene {
    default void postLeave(@NotNull Set<@NotNull Player> leftPlayers) {
        if (leftPlayers.isEmpty() || playerCount() == 0) {
            return;
        }

        List<Player> leftPlayersList = List.copyOf(leftPlayers);
        for (int i = 0; i < leftPlayersList.size(); i++) {
            Player leftPlayer = leftPlayersList.get(i);

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

                if (leftPlayer.isOnline() && !leftPlayer.isViewer(existing)) {
                    leftPlayer.sendPacket(existing.getRemovePlayerToList());
                }
            }

            for (int j = i + 1; j < leftPlayersList.size(); j++) {
                Player otherLeftPlayer = leftPlayersList.get(j);
                if (hasPlayer(otherLeftPlayer)) {
                    continue;
                }

                if (!leftPlayer.isOnline() && !otherLeftPlayer.isOnline()) {
                    continue;
                }

                if (leftPlayer.isOnline() && otherLeftPlayer.isOnline()) {
                    if (!leftPlayer.isViewer(otherLeftPlayer)) {
                        leftPlayer.sendPacket(otherLeftPlayer.getRemovePlayerToList());
                    }

                    if (!otherLeftPlayer.isViewer(leftPlayer)) {
                        otherLeftPlayer.sendPacket(removalPacket);
                    }

                    continue;
                }

                if (leftPlayer.isOnline()) {
                    leftPlayer.sendPacket(otherLeftPlayer.getRemovePlayerToList());
                    continue;
                }

                otherLeftPlayer.sendPacket(removalPacket);
            }
        }
    }
}
