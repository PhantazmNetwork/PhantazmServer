package org.phantazm.zombies.player.state.revive;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.ZombiesPlayerMeta;

import java.util.*;
import java.util.function.Predicate;

public class NearbyReviverPredicate implements Predicate<ZombiesPlayer> {

    private final PlayerView playerView;

    private final double reviveRadius;

    public NearbyReviverPredicate(@NotNull PlayerView playerView, double reviveRadius) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
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

        ZombiesPlayerMeta meta = revivingPlayer.module().getMeta();
        if (meta.isReviving()) {
            return false;
        }

        Optional<Player> reviverPlayerOptional = revivingPlayer.getPlayer();
        if (reviverPlayerOptional.isEmpty()) {
            return false;
        }
        Player player = reviverPlayerOptional.get();

        return player.getPosition().distance(knockedPosition) <= reviveRadius;
    }
}
