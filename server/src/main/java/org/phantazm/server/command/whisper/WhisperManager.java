package org.phantazm.server.command.whisper;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WhisperManager {

    private final Map<UUID, UUID> previousConverser = new HashMap<>();

    private final ConsoleSender consoleSender;

    private final ConnectionManager connectionManager;

    private final WhisperConfig whisperConfig;

    private final MiniMessage miniMessage;

    public WhisperManager(@NotNull ConnectionManager connectionManager, @NotNull ConsoleSender consoleSender,
        @NotNull WhisperConfig whisperConfig, @NotNull MiniMessage miniMessage) {
        this.connectionManager = Objects.requireNonNull(connectionManager);
        this.consoleSender = Objects.requireNonNull(consoleSender);
        this.whisperConfig = Objects.requireNonNull(whisperConfig);
        this.miniMessage = Objects.requireNonNull(miniMessage);
    }

    public void whisper(@NotNull Audience sender, @NotNull Audience target, @NotNull String message) {
        TagResolver senderPlaceholder = Placeholder.component("sender", resolveDisplayName(sender));
        TagResolver targetPlaceholder = Placeholder.component("target", resolveDisplayName(target));
        TagResolver messagePlaceholder = Placeholder.unparsed("message", message);

        Component toTarget =
            miniMessage.deserialize(whisperConfig.toTargetFormat(), senderPlaceholder, targetPlaceholder,
                messagePlaceholder);
        target.sendMessage(toTarget);

        Component toSender =
            miniMessage.deserialize(whisperConfig.toSenderFormat(), senderPlaceholder, targetPlaceholder,
                messagePlaceholder);
        sender.sendMessage(toSender);

        UUID senderUUID = resolveUUID(sender);
        UUID targetUUID = resolveUUID(target);

        if (senderUUID != null && targetUUID != null) {
            previousConverser.put(targetUUID, senderUUID);
            previousConverser.put(senderUUID, targetUUID);
        }
    }

    public @NotNull Optional<Audience> getLastConverser(@NotNull Audience audience) {
        UUID audienceUUID = resolveUUID(audience);
        if (audienceUUID == null) {
            return Optional.empty();
        }

        UUID lastConverserUUID = previousConverser.get(audienceUUID);
        if (lastConverserUUID == null) {
            return Optional.empty();
        }

        if (lastConverserUUID.equals(Identity.nil().uuid())) {
            return Optional.of(consoleSender);
        }

        return Optional.ofNullable(connectionManager.getPlayer(lastConverserUUID));
    }

    private Component resolveDisplayName(Audience audience) {
        if (audience instanceof Player player) {
            Component displayName = player.getDisplayName();
            if (displayName != null) {
                return displayName.colorIfAbsent(whisperConfig.fallbackNameColor());
            }

            return Component.text(player.getUsername(), whisperConfig.fallbackNameColor());
        }

        if (audience instanceof ConsoleSender) {
            return whisperConfig.consoleName();
        }

        Optional<Component> displayName = audience.get(Identity.DISPLAY_NAME);
        return displayName.map(component -> component.colorIfAbsent(whisperConfig.fallbackNameColor()))
            .orElse(whisperConfig.defaultName());
    }

    private @Nullable UUID resolveUUID(Audience audience) {
        if (audience instanceof Player player) {
            return player.getUuid();
        } else if (audience instanceof ConsoleSender) {
            return consoleSender.identity().uuid();
        }

        return null;
    }

}
