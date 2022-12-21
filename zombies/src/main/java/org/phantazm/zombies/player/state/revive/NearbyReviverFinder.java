package org.phantazm.zombies.player.state.revive;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.ZombiesPlayerMeta;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;

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

            ZombiesPlayerMeta meta = zombiesPlayer.getModule().getMeta();
            if (!(meta.isCanRevive() && !meta.isReviving())) {
                continue;
            }

            Optional<Player> reviverPlayerOptional = zombiesPlayer.getPlayer();
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
