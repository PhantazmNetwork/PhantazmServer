package com.github.phantazmnetwork.phantazmserver.feature.chat.channel;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Container for {@link ChatChannel}s
 */
public interface ChatChannelHolder {

    /**
     * Creates a default {@link ChatChannelHolder}
     * @return A new {@link ChatChannelHolder}
     */
    static @NotNull ChatChannelHolder defaultChatChannelHolder() {
        return new ChatChannelHolder() {

            private final Map<Key, ChatChannel> channels = new HashMap<>();

            @Override
            public void registerChannel(@NotNull Key key, @NotNull ChatChannel chatChannel) {
                if (getChannel(key).isPresent()) {
                    throw new IllegalStateException("A chat channel is already registered for key " + key.asString() +
                            "!");
                }

                channels.put(key, chatChannel);
            }

            @Override
            public void unregisterChannel(@NotNull Key key) {
                if (getChannel(key).isEmpty()) {
                    throw new IllegalStateException("No chat channel is registered for key " + key.asString() + "!");
                }

                channels.remove(key);
            }

            @Override
            public @NotNull Optional<ChatChannel> getChannel(@NotNull Key key) {
                return Optional.ofNullable(channels.get(key));
            }

        };
    }

    /**
     * Registers a {@link ChatChannel}
     * @param key The {@link Key} to register the {@link ChatChannel} with
     * @param chatChannel The {@link ChatChannel} to add
     * @throws IllegalStateException If a {@link ChatChannel} is already registered with the associated {@link Key}
     */
    void registerChannel(@NotNull Key key, @NotNull ChatChannel chatChannel);

    /**
     * Unregisters a {@link ChatChannel}
     * @param key The {@link Key} to unregister the {@link ChatChannel} with
     * @throws IllegalStateException If no {@link ChatChannel} is registered with the associated {@link Key}
     */
    void unregisterChannel(@NotNull Key key);

    /**
     * Gets a {@link ChatChannel}
     * @param key The {@link Key} of the {@link ChatChannel}
     * @return An {@link Optional} containing the {@link ChatChannel}
     */
    @NotNull Optional<ChatChannel> getChannel(@NotNull Key key);

}
