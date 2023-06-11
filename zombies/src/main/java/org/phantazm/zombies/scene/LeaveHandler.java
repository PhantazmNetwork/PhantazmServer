package org.phantazm.zombies.scene;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.RouteResult;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.NoContext;
import org.phantazm.zombies.stage.Stage;
import org.phantazm.zombies.stage.StageTransition;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class LeaveHandler {

    private final StageTransition stageTransition;

    private final Map<? super UUID, ? extends PlayerView> players;

    private final Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers;

    public LeaveHandler(@NotNull StageTransition stageTransition, @NotNull Map<? super UUID, ? extends PlayerView> players,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers) {
        this.stageTransition = Objects.requireNonNull(stageTransition, "stageTransition");
        this.players = Objects.requireNonNull(players, "players");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
    }

    public RouteResult leave(@NotNull Iterable<UUID> leavers) {
        for (UUID leaver : leavers) {
            if (!players.containsKey(leaver)) {
                return new RouteResult(false,
                        Component.text("Not all players are within the scene.", NamedTextColor.RED));
            }
        }

        for (UUID leaver : leavers) {
            players.remove(leaver);

            Stage stage = stageTransition.getCurrentStage();
            ZombiesPlayer zombiesPlayer;
            if (stage == null || !stage.hasPermanentPlayers()) {
                zombiesPlayer = zombiesPlayers.remove(leaver);
                zombiesPlayer.end();
            }
            else {
                zombiesPlayer = zombiesPlayers.get(leaver);

                if (zombiesPlayer != null) {
                    zombiesPlayer.setState(ZombiesPlayerStateKeys.QUIT, NoContext.INSTANCE);
                }
            }
        }

        return RouteResult.SUCCESSFUL;
    }

}
