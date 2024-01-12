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
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.TagUtils;
import org.phantazm.core.player.PlayerViewProvider;

import java.util.*;

public class WhisperManager {
    private static final Tag<UUID> LAST_MESSAGED_TAG = Tag.UUID(TagUtils.uniqueTagName());
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final ConsoleSender consoleSender;
    private final ConnectionManager connectionManager;
    private final WhisperConfig whisperConfig;

    private record TagEntry(TagHandler handler,
        UUID self) {
    }

    public WhisperManager(@NotNull ConnectionManager connectionManager, @NotNull ConsoleSender consoleSender,
        @NotNull WhisperConfig whisperConfig) {
        this.connectionManager = Objects.requireNonNull(connectionManager);
        this.consoleSender = Objects.requireNonNull(consoleSender);
        this.whisperConfig = Objects.requireNonNull(whisperConfig);
    }

    public void whisper(@NotNull Audience sender, @NotNull Audience target, @NotNull String message) {
        TagResolver senderPlaceholder = Placeholder.component("sender", resolveDisplayName(sender));
        TagResolver targetPlaceholder = Placeholder.component("target", resolveDisplayName(target));
        TagResolver messagePlaceholder = Placeholder.unparsed("message", message);

        Component toTarget =
            MINI_MESSAGE.deserialize(whisperConfig.toTargetFormat(), senderPlaceholder, targetPlaceholder,
                messagePlaceholder);
        target.sendMessage(toTarget);

        Component toSender =
            MINI_MESSAGE.deserialize(whisperConfig.toSenderFormat(), senderPlaceholder, targetPlaceholder,
                messagePlaceholder);
        sender.sendMessage(toSender);

        TagEntry senderTags = resolveTagEntry(sender);
        TagEntry targetTags = resolveTagEntry(target);

        if (senderTags != null && targetTags != null) {
            senderTags.handler.setTag(LAST_MESSAGED_TAG, targetTags.self);
            targetTags.handler.setTag(LAST_MESSAGED_TAG, senderTags.self);
        }
    }

    public @NotNull Optional<Audience> getLastConverser(@NotNull Audience audience) {
        TagEntry audienceEntry = resolveTagEntry(audience);
        if (audienceEntry == null) {
            return Optional.empty();
        }

        UUID lastConverserUUID = audienceEntry.handler.getTag(LAST_MESSAGED_TAG);
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

    private @Nullable TagEntry resolveTagEntry(Audience audience) {
        if (audience instanceof Player player) {
            return new TagEntry(PlayerViewProvider.Global.instance().fromPlayer(player).tagHandler(), player.getUuid());
        } else if (audience instanceof ConsoleSender) {
            return new TagEntry(consoleSender.tagHandler(), consoleSender.identity().uuid());
        }

        return null;
    }

}
