package com.github.zapv3.feature.chat.channel;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Tracker of an {@link Audience}'s {@link ConnectedChannelSettings}
 */
public interface ChatChannelTracker {

    /**
     * Default implementation of a {@link ChatChannelTracker}
     */
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

    /**
     * Gets the {@link ConnectedChannelSettings} for an {@link Audience}
     * @param audience The {@link Audience} to get the {@link ConnectedChannelSettings} of
     * @return An {@link Optional} of the {@link Audience}'s {@link ConnectedChannelSettings}
     */
    @NotNull Optional<ConnectedChannelSettings> getConnectedChannelSettings(@NotNull Audience audience);

    /**
     * Sets the {@link ConnectedChannelSettings} for an {@link Audience}
     * @param audience The {@link Audience} to set the {@link ConnectedChannelSettings} of
     * @param settings The {@link ConnectedChannelSettings} to use
     */
    void setConnectedChannelSettings(@NotNull Audience audience, @NotNull ConnectedChannelSettings settings);

    /**
     * Stops tracking an {@link Audience}
     * @param audience The {@link Audience} to stop tracking
     */
    void stopTracking(@NotNull Audience audience);

    /**
     * Handles a message from an {@link Audience}
     * @param audience The {@link Audience} that sent the message
     * @param message The message that the {@link Audience} sent
     */
    void messageFrom(@NotNull Audience audience, @NotNull Component message);

}
