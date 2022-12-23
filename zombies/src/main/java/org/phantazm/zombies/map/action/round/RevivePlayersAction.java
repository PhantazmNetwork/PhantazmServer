package org.phantazm.zombies.map.action.round;

import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.action.Action;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.NoContext;
import org.phantazm.zombies.player.state.revive.KnockedPlayerState;

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
            if (zombiesPlayer.isDead()) {
                zombiesPlayer.setState(ZombiesPlayerStateKeys.ALIVE, NoContext.INSTANCE);
                zombiesPlayer.getPlayer().ifPresent(player -> player.teleport(respawnPos));
            }
            else if (zombiesPlayer.getModule().getStateSwitcher()
                    .getState() instanceof KnockedPlayerState knockedPlayerState) {
                knockedPlayerState.getReviveHandler().setReviver(null);
                zombiesPlayer.setState(ZombiesPlayerStateKeys.ALIVE, NoContext.INSTANCE);
            }
        }
    }
}
