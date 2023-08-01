package org.phantazm.core.friend;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class FriendNotification {

    private static final Logger LOGGER = LoggerFactory.getLogger(FriendNotification.class);

    private final FriendNotificationConfig config;

    private final MiniMessage miniMessage;

    public FriendNotification(@NotNull FriendNotificationConfig config, @NotNull MiniMessage miniMessage) {
        this.config = Objects.requireNonNull(config, "config");
        this.miniMessage = Objects.requireNonNull(miniMessage, "miniMessage");
    }

    public void notifyRequest(@NotNull PlayerView requester, @NotNull PlayerView target) {
        target.getDisplayName().whenComplete((displayName, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to get display name for {}", target.getUUID());
                return;
            }

            requester.getPlayer().ifPresent(player -> {
                TagResolver targetPlaceholder = Placeholder.component("target", displayName);
                Component message = miniMessage.deserialize(config.requestToRequesterFormat(), targetPlaceholder);
                player.sendMessage(message);
            });
        });

        requester.getDisplayName().whenComplete((displayName, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to get display name for {}", target.getUUID());
                return;
            }

            target.getPlayer().ifPresent(player -> {
                TagResolver targetPlaceholder = Placeholder.component("requester", displayName);
                Component message = miniMessage.deserialize(config.requestToTargetFormat(), targetPlaceholder);
                player.sendMessage(message);
            });
        });
    }

    public void notifyAccept(@NotNull PlayerView requester, @NotNull PlayerView target) {
        target.getDisplayName().whenComplete((displayName, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to get display name for {}", target.getUUID());
                return;
            }

            requester.getPlayer().ifPresent(player -> {
                TagResolver targetPlaceholder = Placeholder.component("target", displayName);
                Component message = miniMessage.deserialize(config.acceptToRequesterFormat(), targetPlaceholder);
                player.sendMessage(message);
            });
        });

        requester.getDisplayName().whenComplete((displayName, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to get display name for {}", target.getUUID());
                return;
            }

            target.getPlayer().ifPresent(player -> {
                TagResolver targetPlaceholder = Placeholder.component("requester", displayName);
                Component message = miniMessage.deserialize(config.acceptToTargetFormat(), targetPlaceholder);
                player.sendMessage(message);
            });
        });
    }

    public void notifyExpiry(@NotNull PlayerView requester, @NotNull PlayerView target) {
        target.getDisplayName().whenComplete((displayName, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to get display name for {}", target.getUUID());
                return;
            }

            requester.getPlayer().ifPresent(player -> {
                TagResolver targetPlaceholder = Placeholder.component("target", displayName);
                Component message = miniMessage.deserialize(config.expiryToRequesterFormat(), targetPlaceholder);
                player.sendMessage(message);
            });
        });

        requester.getDisplayName().whenComplete((displayName, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to get display name for {}", target.getUUID());
                return;
            }

            target.getPlayer().ifPresent(player -> {
                TagResolver targetPlaceholder = Placeholder.component("requester", displayName);
                Component message = miniMessage.deserialize(config.expiryToTargetFormat(), targetPlaceholder);
                player.sendMessage(message);
            });
        });
    }

    public void notifyRemove(@NotNull PlayerView remover, @NotNull PlayerView target) {
        target.getDisplayName().whenComplete((displayName, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to get display name for {}", target.getUUID());
                return;
            }

            remover.getPlayer().ifPresent(player -> {
                TagResolver targetPlaceholder = Placeholder.component("target", displayName);
                Component message = miniMessage.deserialize(config.removeToRemoverFormat(), targetPlaceholder);
                player.sendMessage(message);
            });
        });

        remover.getDisplayName().whenComplete((displayName, throwable) -> {
            if (throwable != null) {
                LOGGER.warn("Failed to get display name for {}", target.getUUID());
                return;
            }

            target.getPlayer().ifPresent(player -> {
                TagResolver targetPlaceholder = Placeholder.component("remover", displayName);
                Component message = miniMessage.deserialize(config.removeToTargetFormat(), targetPlaceholder);
                player.sendMessage(message);
            });
        });
    }

}
