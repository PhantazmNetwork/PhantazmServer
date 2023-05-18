package org.phantazm.zombies.player.state.revive;

import com.github.steanky.toolkit.collection.Wrapper;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.ZombiesPlayerMeta;

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
        Player knockedPlayer = knockedPlayerOptional.get();
        Point knockedPosition = knockedPlayer.getPosition();

        Instance instance = knockedPlayer.getInstance();
        if (instance == null) {
            return null;
        }

        Wrapper<ZombiesPlayer> playerWrapper = Wrapper.ofNull();
        instance.getEntityTracker()
                .nearbyEntitiesUntil(knockedPosition, reviveRadius, EntityTracker.Target.PLAYERS, candidate -> {
                    UUID candidateUUID = candidate.getUuid();
                    ZombiesPlayer zombiesPlayer = zombiesPlayers.get(candidateUUID);
                    if (zombiesPlayer == null) {
                        return false;
                    }

                    if (candidateUUID.equals(playerView.getUUID())) {
                        return false;
                    }

                    if (!zombiesPlayer.isAlive()) {
                        return false;
                    }

                    ZombiesPlayerMeta meta = zombiesPlayer.module().getMeta();
                    if (!(meta.isCanRevive() && !meta.isReviving())) {
                        return false;
                    }

                    if (!meta.isCrouching()) {
                        return false;
                    }

                    Optional<Player> reviverPlayerOptional = zombiesPlayer.getPlayer();
                    if (reviverPlayerOptional.isEmpty()) {
                        return false;
                    }

                    Player player = reviverPlayerOptional.get();
                    if (player.getPosition().distance(knockedPosition) <= reviveRadius) {
                        playerWrapper.set(zombiesPlayer);
                        return true;
                    }

                    return false;
                });

        return playerWrapper.get();
    }
}
