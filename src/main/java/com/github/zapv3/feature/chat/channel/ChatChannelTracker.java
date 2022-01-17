package com.github.zapv3.feature.chat.channel;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface ChatChannelTracker {

    static @NotNull ChatChannelTracker defaultTracker() {
        return new ChatChannelTracker() {

            private final Map<Audience, ConnectedChannelSettings> connectedChannelSettingsMap = new HashMap<>();

            @Override
            public @NotNull Optional<ConnectedChannelSettings> getConnectedChannelSettings(@NotNull Audience audience) {
                return Optional.ofNullable(connectedChannelSettingsMap.get(audience));
            }

            @Override
            public void setConnectedChannelSettings(@NotNull Audience audience,
                                                    @NotNull ConnectedChannelSettings settings) {
                connectedChannelSettingsMap.put(audience, settings);
            }

            @Override
            public void stopTracking(@NotNull Audience audience) {
                connectedChannelSettingsMap.remove(audience);
            }

            @Override
            public void messageFrom(@NotNull Audience audience, @NotNull Component message) {
                connectedChannelSettingsMap.get(audience).messageChannel().sendMessage(message);
            }
        };
    }

    @NotNull Optional<ConnectedChannelSettings> getConnectedChannelSettings(@NotNull Audience audience);

    void setConnectedChannelSettings(@NotNull Audience audience, @NotNull ConnectedChannelSettings settings);

    void stopTracking(@NotNull Audience audience);

    void messageFrom(@NotNull Audience audience, @NotNull Component message);

}
