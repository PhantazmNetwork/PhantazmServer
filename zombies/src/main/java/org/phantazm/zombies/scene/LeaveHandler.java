package org.phantazm.zombies.scene;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.RouteResult;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.QuitPlayerStateContext;
import org.phantazm.zombies.stage.Stage;
import org.phantazm.zombies.stage.StageTransition;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class LeaveHandler {
    private final StageTransition stageTransition;

    private final Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers;

    public LeaveHandler(@NotNull StageTransition stageTransition,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers) {
        this.stageTransition = Objects.requireNonNull(stageTransition, "stageTransition");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
    }

    public RouteResult leave(@NotNull Iterable<UUID> leavers) {
        for (UUID leaver : leavers) {
            if (!zombiesPlayers.containsKey(leaver)) {
                return new RouteResult(false,
                        Component.text("Not all players are within the scene.", NamedTextColor.RED));
            }
        }

        for (UUID leaver : leavers) {
            zombiesPlayers.remove(leaver);

            Stage stage = stageTransition.getCurrentStage();

            ZombiesPlayer zombiesPlayer = zombiesPlayers.get(leaver);
            if (zombiesPlayer == null) {
                continue;
            }

            if (stage != null) {
                stage.onLeave(zombiesPlayer);
            }

            if (stage == null || !stage.hasPermanentPlayers()) {
                zombiesPlayers.remove(leaver);
                zombiesPlayer.end();
            }

            zombiesPlayer.setState(ZombiesPlayerStateKeys.QUIT, new QuitPlayerStateContext(true));
        }

        return RouteResult.SUCCESSFUL;
    }

}
