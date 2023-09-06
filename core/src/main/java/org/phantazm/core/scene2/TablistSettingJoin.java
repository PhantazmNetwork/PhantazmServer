package org.phantazm.core.scene2;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.Utils;
import org.phantazm.core.player.PlayerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface TablistSettingJoin<T extends InstanceScene> extends Join<T> {
    @Override
    default void join(@NotNull T scene) {
        if (players().isEmpty()) {
            return;
        }

        List<Player> players = new ArrayList<>(players().size());
        for (PlayerView view : players()) {
            Optional<Player> playerOptional = view.getPlayer();
            if (playerOptional.isEmpty()) {
                continue;
            }

            players.add(playerOptional.get());
        }

        Instance newInstance = scene.instance();
        for (Player player : players) {
            Utils.handleInstanceTransfer(player.getInstance(), newInstance, player, newInstancePlayer -> true);
        }

        if (players.size() == 1) {
            return;
        }

        for (int i = 1; i < players.size(); i++) {
            Player first = players.get(i - 1);
            ServerPacket firstAdd = first.getAddPlayerToList();

            for (int j = i; j < players.size(); j++) {
                Player second = players.get(j);
                if (first.getInstance() != newInstance && second.getInstance() != newInstance) {
                    first.sendPacket(second.getAddPlayerToList());
                    second.sendPacket(firstAdd);
                }
            }
        }
    }
}
