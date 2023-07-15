package org.phantazm.core.game.scene;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Predicate;

public class Utils {
    /**
     * Handles player transfer between instances, sending list packets. Will optionally add this player to the tab
     * list of other players in the instance. This method must be called <i>before</i> spawn packets are sent to players
     * in the target instance.
     *
     * @param oldInstance   the old instance; is {@code null} if the player is logging in for the first time
     * @param newInstance   the new instance, if this is the same object as oldInstance, this method will do nothing
     * @param player        the player
     * @param playersToShow a predicate to test whether a player in {@code newInstance} should receive a tablist packet
     */
    public static void handleInstanceTransfer(@Nullable Instance oldInstance, @NotNull Instance newInstance,
            @NotNull Player player, @NotNull Predicate<? super Player> playersToShow) {
        if (newInstance == oldInstance) {
            return;
        }

        ServerPacket playerRemove = player.getRemovePlayerToList();
        ServerPacket playerAdd = player.getAddPlayerToList();

        if (oldInstance != null) {
            for (Player oldPlayer : oldInstance.getEntityTracker().entities(EntityTracker.Target.PLAYERS)) {
                if (oldPlayer == player) {
                    continue;
                }

                oldPlayer.sendPacket(playerRemove);
                player.sendPacket(oldPlayer.getRemovePlayerToList());
            }
        }

        Set<Player> instancePlayers = newInstance.getEntityTracker().entities(EntityTracker.Target.PLAYERS);
        for (Player newInstancePlayer : instancePlayers) {
            if (newInstancePlayer == player) {
                continue;
            }

            player.sendPacket(newInstancePlayer.getAddPlayerToList());

            if (playersToShow.test(newInstancePlayer)) {
                newInstancePlayer.sendPacket(playerAdd);
            }
        }
    }

    /**
     * Handles player transfer between instances, sending list packets. Will also notify players in the new instance
     * (the player's current instance).
     *
     * @param oldInstance the old instance; is {@code null} if the player is logging in for the first time
     * @param player      the player
     */
    public static void handleInstanceTransfer(@Nullable Instance oldInstance, @NotNull Instance newInstance,
            @NotNull Player player) {
        handleInstanceTransfer(oldInstance, newInstance, player, newInstancePlayer -> true);
    }
}
