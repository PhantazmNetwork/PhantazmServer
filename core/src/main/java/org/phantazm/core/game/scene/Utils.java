package org.phantazm.core.game.scene;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Utils {
    /**
     * Handles player transfer between instances, updating viewables as appropriate and sending list packets.
     *
     * @param oldInstance the old instance; is {@code null} if the player is logging in for the first time
     * @param player      the player
     */
    public static void handleInstanceTransfer(@Nullable Instance oldInstance, @NotNull Player player) {
        Instance newInstance = Objects.requireNonNull(player.getInstance(), "player instance");

        if (newInstance == oldInstance) {
            return;
        }

        if (oldInstance != null) {
            ServerPacket playerRemove = player.getRemovePlayerToList();
            ServerPacket playerAdd = player.getAddPlayerToList();

            for (Player oldPlayer : oldInstance.getEntityTracker().entities(EntityTracker.Target.PLAYERS)) {
                oldPlayer.removeViewer(player);
                player.removeViewer(oldPlayer);

                oldPlayer.sendPacket(playerRemove);
                player.sendPacket(oldPlayer.getRemovePlayerToList());
            }

            for (Player newPlayer : newInstance.getEntityTracker().entities(EntityTracker.Target.PLAYERS)) {
                if (newPlayer == player) {
                    continue;
                }

                newPlayer.addViewer(player);
                player.addViewer(newPlayer);

                newPlayer.sendPacket(playerAdd);
                player.sendPacket(newPlayer.getAddPlayerToList());
            }
        }
        else {
            //player list packets have already been send by the player during init
            for (Player newPlayer : newInstance.getEntityTracker().entities(EntityTracker.Target.PLAYERS)) {
                newPlayer.addViewer(player);
                player.addViewer(newPlayer);
            }
        }
    }
}
