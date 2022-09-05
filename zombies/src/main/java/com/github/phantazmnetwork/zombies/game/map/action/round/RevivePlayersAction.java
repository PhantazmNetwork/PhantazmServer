package com.github.phantazmnetwork.zombies.game.map.action.round;

import com.github.phantazmnetwork.zombies.game.map.Round;
import com.github.phantazmnetwork.zombies.game.map.action.Action;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.game.player.state.KnockedPlayerState;
import com.github.phantazmnetwork.zombies.game.player.state.ZombiesPlayerState;
import com.github.phantazmnetwork.zombies.game.player.state.ZombiesPlayerStateKeys;
import com.github.phantazmnetwork.zombies.game.player.state.context.NoContext;
import com.github.steanky.element.core.annotation.*;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Model("zombies.map.round.action.revive_players")
public class RevivePlayersAction implements Action<Round> {
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;

    private final Pos respawnPos;

    @FactoryMethod
    public RevivePlayersAction(@NotNull @Dependency("zombies.dependency.map_object.player_map")
    Map<? super UUID, ? extends ZombiesPlayer> playerMap,
            @NotNull @Dependency("zombies.dependency.map_object.respawn_pos") Pos respawnPos) {
        this.playerMap = Objects.requireNonNull(playerMap, "playerMap");
        this.respawnPos = Objects.requireNonNull(respawnPos, "respawnPoint");
    }

    @Override
    public void perform(@NotNull Round round) {
        for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
            ZombiesPlayerState state = zombiesPlayer.getModule().getStateSwitcher().getState();
            if (state.key().equals(ZombiesPlayerStateKeys.DEAD.key())) {
                zombiesPlayer.setState(ZombiesPlayerStateKeys.ALIVE, NoContext.INSTANCE);
                zombiesPlayer.getModule().getPlayerView().getPlayer().ifPresent(player -> player.teleport(respawnPos));
            }
            else if (state instanceof KnockedPlayerState knockedPlayerState) {
                knockedPlayerState.setReviver(null);
                zombiesPlayer.setState(ZombiesPlayerStateKeys.ALIVE, NoContext.INSTANCE);
            }
        }
    }
}
