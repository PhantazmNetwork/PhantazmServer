package com.github.zapv3.feature.chat.channel;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface ChatChannelHolder {

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

    void registerChannel(@NotNull Key key, @NotNull ChatChannel chatChannel);

    void unregisterChannel(@NotNull Key key);

    @NotNull Optional<ChatChannel> getChannel(@NotNull Key key);

}
