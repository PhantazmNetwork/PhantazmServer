package com.github.phantazmnetwork.api.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * {@link PlayerContainer} based on {@link UUID}. This will need to be updated by an {@link EventNode}.
 */
public class UUIDPlayerContainer implements PlayerContainer {

    private final Map<UUID, Player> players = new HashMap<>();

    @Override
    public Player getPlayer(@NotNull UUID uuid) {
        return players.get(uuid);
    }

    /**
     * Adds a player to the container.
     * @param player The player to add
     */
    public void addPlayer(@NotNull Player player) {
        players.put(player.getUuid(), player);
    }

    /**
     * Removes a player from the container based on their {@link UUID}.
     * @param uuid The {@link UUID} of the player to remove
     */
    public void removePlayer(@NotNull UUID uuid) {
        players.remove(uuid);
    }

}
