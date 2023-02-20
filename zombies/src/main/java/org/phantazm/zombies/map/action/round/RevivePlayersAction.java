package org.phantazm.zombies.map.action.round;

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
    public RevivePlayersAction(@NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap, @NotNull Pos respawnPos) {
        this.playerMap = Objects.requireNonNull(playerMap, "playerMap");
        this.respawnPos = Objects.requireNonNull(respawnPos, "respawnPoint");
    }

    @Override
    public void perform(@NotNull Round round) {
        for (ZombiesPlayer zombiesPlayer : playerMap.values()) {
            boolean dead = zombiesPlayer.isDead();
            if (dead || zombiesPlayer.isKnocked()) {
                if (dead) {
                    zombiesPlayer.getPlayer().ifPresent(player -> player.teleport(respawnPos));
                }

                zombiesPlayer.setState(ZombiesPlayerStateKeys.ALIVE, NoContext.INSTANCE);
            }
        }
    }
}
