package org.phantazm.zombies.player.state.revive;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class NearbyReviverPredicate implements Predicate<ZombiesPlayer> {

    private final PlayerView playerView;

    private final double reviveRadius;

    public NearbyReviverPredicate(@NotNull PlayerView playerView, double reviveRadius) {
        this.playerView = Objects.requireNonNull(playerView);
        this.reviveRadius = reviveRadius;
    }

    @Override
    public boolean test(ZombiesPlayer revivingPlayer) {
        Optional<Player> knockedPlayerOptional = playerView.getPlayer();
        if (knockedPlayerOptional.isEmpty()) {
            return false;
        }
        Player knockedPlayer = knockedPlayerOptional.get();
        Point knockedPosition = knockedPlayer.getPosition();

        Instance instance = knockedPlayer.getInstance();
        if (instance == null) {
            return false;
        }

        if (revivingPlayer.getUUID().equals(playerView.getUUID()) || !revivingPlayer.canRevive()) {
            return false;
        }

        return revivingPlayer.getPlayer()
            .filter(player -> player.getPosition().distance(knockedPosition) <= reviveRadius).isPresent();

    }
}
