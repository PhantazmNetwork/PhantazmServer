package com.github.zapv3.feature.chat.channel;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.jetbrains.annotations.NotNull;

/**
 * A channel that {@link Audience}s can subscribe themselves to in order to receive messages
 */
public interface ChatChannel extends ForwardingAudience {

    /**
     * Adds an {@link Audience} to the channel
     * @param audience The {@link Audience} to add
     * @return Whether adding the {@link Audience} was successful
     */
    boolean addToChannel(@NotNull Audience audience);

    /**
     * Removes an {@link Audience} from the channel
     * @param audience The {@link Audience} to remove
     * @return Whether remove the {@link Audience} was successful
     */
    boolean removeFromChannel(@NotNull Audience audience);

    /**
     * Checks whether an {@link Audience} is in the channel
     * @param audience The {@link Audience} to check
     * @return Whether the {@link Audience} is in the channel
     */
    boolean isInChannel(@NotNull Audience audience);

}
