package com.github.phantazmnetwork.api.chat.command;

import com.github.phantazmnetwork.api.chat.ChatChannel;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Callback for when a player's chat channel changes.
 */
@FunctionalInterface
public interface ChatChannelChangeCallback {

    /**
     * Called when a player changes their chat channel.
     * @param player The {@link Player} who is changing their chat channel
     * @param channel The {@link Player}'s new chat channel
     */
    void onChannelChanged(@NotNull Player player, @NotNull ChatChannel channel);

}
