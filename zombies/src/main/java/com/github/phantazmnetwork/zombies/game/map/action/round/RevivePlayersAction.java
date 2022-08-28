package com.github.phantazmnetwork.zombies.game.map.action.round;

import com.github.phantazmnetwork.zombies.game.map.Round;
import com.github.phantazmnetwork.zombies.game.map.action.Action;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.game.player.state.KnockedPlayerState;
import com.github.phantazmnetwork.zombies.game.player.state.ZombiesPlayerState;
import com.github.phantazmnetwork.zombies.game.player.state.ZombiesPlayerStateKeys;
import com.github.phantazmnetwork.zombies.game.player.state.context.NoContext;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

@Model("zombies.map.round.action.revive_players")
public class RevivePlayersAction implements Action<Round> {

    @DataObject
    public record Data() {

    }

    private final Collection<ZombiesPlayer> zombiesPlayers;

    private final Data data;

    private final Pos respawnPoint;

    @FactoryMethod
    public RevivePlayersAction(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.players.collection") Collection<ZombiesPlayer> zombiesPlayers,
            @NotNull @Dependency(value = "minestom.point.pos", name = "zombies.dependency.map.respawn_point.minestom")
            Pos respawnPoint) {
        this.data = Objects.requireNonNull(data, "data");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.respawnPoint = Objects.requireNonNull(respawnPoint, "respawnPoint");
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                return ConfigNode.of();
            }
        };
    }

    @Override
    public void perform(@NotNull Round round) {
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            ZombiesPlayerState state = zombiesPlayer.getStateSwitcher().getState();
            if (state.key().equals(ZombiesPlayerStateKeys.DEAD.key())) {
                zombiesPlayer.setState(ZombiesPlayerStateKeys.ALIVE, NoContext.INSTANCE);
                zombiesPlayer.getPlayerView().getPlayer().ifPresent(player -> {
                    player.teleport(respawnPoint);
                });
            }
            else if (state instanceof KnockedPlayerState knockedPlayerState) {
                knockedPlayerState.setReviver(null);
                zombiesPlayer.setState(ZombiesPlayerStateKeys.ALIVE, NoContext.INSTANCE);
            }
        }
    }
}
