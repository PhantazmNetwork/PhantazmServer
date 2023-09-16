package org.phantazm.core.scene2;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.player.PlayerView;

import java.util.Set;
import java.util.UUID;

/**
 * A {@link Scene} that can accept spectators. Spectators are considered a "part of" the scene (they are still present
 * in {@link Scene#playersView()} etc.) but are generally not visible to non-spectators.
 */
public interface WatchableScene extends Scene {
    void joinSpectators(@NotNull Set<@NotNull PlayerView> players, boolean ghost);

    @NotNull @UnmodifiableView Set<@NotNull PlayerView> spectatorsView();

    default int spectatorsCount() {
        return spectatorsView().size();
    }

    default boolean hasSpectator(@NotNull PlayerView playerView) {
        return spectatorsView().contains(playerView);
    }

    default boolean hasSpectator(@NotNull Player player) {
        return spectatorsView().contains(PlayerView.lookup(player.getUuid()));
    }

    default boolean hasSpectator(@NotNull UUID uuid) {
        return spectatorsView().contains(PlayerView.lookup(uuid));
    }
}
