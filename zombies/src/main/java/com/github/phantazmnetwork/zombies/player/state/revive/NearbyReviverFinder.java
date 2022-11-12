package com.github.phantazmnetwork.zombies.player.state.revive;

import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.player.state.ZombiesPlayerStateKeys;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class NearbyReviverFinder implements Supplier<ZombiesPlayer> {

    private final Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers;

    private final PlayerView playerView;

    private final double reviveRadius;

    public NearbyReviverFinder(@NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers,
            @NotNull PlayerView playerView, double reviveRadius) {
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.reviveRadius = reviveRadius;
    }

    @Override
    public ZombiesPlayer get() {
        Optional<Player> knockedPlayerOptional = playerView.getPlayer();
        if (knockedPlayerOptional.isEmpty()) {
            return null;
        }
        Point knockedPosition = knockedPlayerOptional.get().getPosition();

        for (ZombiesPlayer zombiesPlayer : zombiesPlayers.values()) {
            if (zombiesPlayer.getModule().getPlayerView().getUUID().equals(playerView.getUUID())) {
                continue;
            }
            if (!zombiesPlayer.getModule().getStateSwitcher().getState().key()
                    .equals(ZombiesPlayerStateKeys.ALIVE.key())) {
                continue;
            }

            Optional<Player> reviverPlayerOptional = zombiesPlayer.getModule().getPlayerView().getPlayer();
            if (reviverPlayerOptional.isEmpty()) {
                continue;
            }

            Player player = reviverPlayerOptional.get();
            if (player.getPosition().distance(knockedPosition) <= reviveRadius) {
                return zombiesPlayer;
            }
        }

        return null;
    }
}
