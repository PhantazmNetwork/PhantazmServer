package org.phantazm.zombies.scene;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.RouteResult;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.QuitPlayerStateContext;
import org.phantazm.zombies.stage.Stage;
import org.phantazm.zombies.stage.StageTransition;

import java.util.*;


public class LeaveHandler {
    private final StageTransition stageTransition;

    private final Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers;

    public LeaveHandler(@NotNull StageTransition stageTransition,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers) {
        this.stageTransition = Objects.requireNonNull(stageTransition, "stageTransition");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
    }

    /**
     * Handles updating internal state when a player leaves mid-game; not necessary to call at the end of a game as it
     * will get destroyed regardless. Does not transfer players to another scene; this ONLY updates internal state and
     * reports whether or the operation was "successful" (if there were players removed from the internal state).
     *
     * @param leavers the players who are leaving
     * @return a {@link RouteResult} representing the operation
     */
    public @NotNull RouteResult leave(@NotNull Iterable<UUID> leavers) {
        List<Pair<UUID, ZombiesPlayer>> leavingPlayers = new ArrayList<>();

        boolean failure = false;
        for (UUID leaver : leavers) {
            ZombiesPlayer zombiesPlayer = zombiesPlayers.get(leaver);
            if (zombiesPlayer == null) {
                failure = true;
                continue;
            }

            leavingPlayers.add(Pair.of(leaver, zombiesPlayer));
        }

        Stage stage = stageTransition.getCurrentStage();
        for (Pair<UUID, ZombiesPlayer> pair : leavingPlayers) {
            ZombiesPlayer zombiesPlayer = pair.right();
            if (stage != null) {
                stage.onLeave(zombiesPlayer);
            }

            if (stage == null || !stage.hasPermanentPlayers()) {
                zombiesPlayers.remove(pair.left());
                zombiesPlayer.setState(ZombiesPlayerStateKeys.QUIT, new QuitPlayerStateContext(true));
                zombiesPlayer.end();
            }
        }

        return failure ? new RouteResult(false,
                Component.text("Not all players are within the scene.", NamedTextColor.RED)) : RouteResult.SUCCESSFUL;
    }

}
