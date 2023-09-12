package org.phantazm.core.scene2;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;

import java.util.*;

public interface TablistLocalJoin<T extends Scene> extends Join<T> {
    enum Type {
        BOTH_JOINING,
        FIRST_JOINING,
        SECOND_JOINING
    }

    enum ViewResult {
        FIRST_SEES_SECOND,
        SECOND_SEES_FIRST,
        NEITHER_SEES,
        BOTH_SEE;

        private void sendAdd(@NotNull PlayerView firstView, @NotNull PlayerView secondView) {
            Optional<Player> firstOptional = firstView.getPlayer();
            Optional<Player> secondOptional = secondView.getPlayer();
            if (firstOptional.isEmpty() || secondOptional.isEmpty()) {
                return;
            }

            Player first = firstOptional.get();
            Player second = secondOptional.get();

            switch (this) {
                case FIRST_SEES_SECOND -> first.sendPacket(second.getAddPlayerToList());
                case SECOND_SEES_FIRST -> second.sendPacket(first.getAddPlayerToList());
                case BOTH_SEE -> {
                    first.sendPacket(second.getAddPlayerToList());
                    second.sendPacket(first.getAddPlayerToList());
                }
            }
        }
    }

    @Override
    default void join(@NotNull T scene) {
        Set<PlayerView> players = playerViews();
        if (players.isEmpty()) {
            return;
        }

        List<PlayerView> joiningPlayers = List.copyOf(players);
        Set<PlayerView> existingPlayers = scene.playersView();

        for (int i = 0; i < players.size(); i++) {
            PlayerView first = joiningPlayers.get(i);
            boolean firstInScene = scene.hasPlayer(first);

            //send tablist packets for other players in this join
            for (int j = i + 1; j < joiningPlayers.size(); j++) {
                PlayerView second = joiningPlayers.get(j);
                boolean secondInScene = scene.hasPlayer(second);

                //both players are in the scene already
                if (firstInScene && secondInScene) {
                    continue;
                }

                //both players are joining the scene
                if (!firstInScene && !secondInScene) {
                    visibility(first, second, Type.BOTH_JOINING).sendAdd(first, second);
                    continue;
                }

                visibility(first, second, !firstInScene ? Type.FIRST_JOINING : Type.SECOND_JOINING)
                    .sendAdd(first, second);
            }

            if (firstInScene) {
                continue;
            }

            for (PlayerView scenePlayer : existingPlayers) {
                //if this online player is in the join set, they will be separately handled in the loop above
                if (first == scenePlayer || players.contains(scenePlayer)) {
                    continue;
                }

                visibility(first, scenePlayer, Type.FIRST_JOINING).sendAdd(first, scenePlayer);
            }
        }
    }

    default @NotNull ViewResult visibility(@NotNull PlayerView first, @NotNull PlayerView second, @NotNull Type type) {
        return ViewResult.BOTH_SEE;
    }
}
