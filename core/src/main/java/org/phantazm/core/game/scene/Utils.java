package org.phantazm.core.game.scene;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Utils {
    /**
     * Handles player transfer between instances, sending list packets. Will optionally add this player to the tab
     * list of other players in the instance.
     *
     * @param oldInstance the old instance; is {@code null} if the player is logging in for the first time
     * @param player      the player
     * @param showPlayers whether to send packets to players in the new instance (the player's current instance)
     */
    public static void handleInstanceTransfer(@Nullable Instance oldInstance, @NotNull Player player,
            boolean showPlayers) {
        Instance newInstance = Objects.requireNonNull(player.getInstance(), "player instance");
        if (newInstance == oldInstance) {
            return;
        }

        ServerPacket playerRemove = player.getRemovePlayerToList();
        ServerPacket playerAdd = player.getAddPlayerToList();

        if (oldInstance != null) {
            for (Player oldPlayer : oldInstance.getEntityTracker().entities(EntityTracker.Target.PLAYERS)) {
                oldPlayer.sendPacket(playerRemove);
                player.sendPacket(oldPlayer.getRemovePlayerToList());
            }
        }

        for (Player newInstancePlayer : newInstance.getEntityTracker().entities(EntityTracker.Target.PLAYERS)) {
            if (newInstancePlayer == player) {
                continue;
            }

            player.sendPacket(newInstancePlayer.getAddPlayerToList());

            if (showPlayers) {
                newInstancePlayer.sendPacket(playerAdd);
            }

            MinecraftServer.getSchedulerManager().buildTask(() -> {
                player.updateNewViewer(newInstancePlayer);

                if (showPlayers) {
                    newInstancePlayer.updateNewViewer(player);
                }
            }).delay(20, TimeUnit.SERVER_TICK).schedule();
        }
    }

    /**
     * Handles player transfer between instances, sending list packets. Will also notify players in the new instance
     * (the player's current instance).
     *
     * @param oldInstance the old instance; is {@code null} if the player is logging in for the first time
     * @param player      the player
     */
    public static void handleInstanceTransfer(@NotNull Instance oldInstance, @NotNull Player player) {
        handleInstanceTransfer(oldInstance, player, true);
    }
}
