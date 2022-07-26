package com.github.phantazmnetwork.core.chat;

import com.github.phantazmnetwork.core.player.PlayerViewProvider;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerChatEvent;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ChatChannel} that only sends message to the message's sender.
 */
public class SelfChatChannel extends BasicChatChannel {

    /**
     * Creates a {@link SelfChatChannel}.
     *
     * @param viewProvider The {@link SelfChatChannel}'s {@link PlayerViewProvider}
     */
    public SelfChatChannel(@NotNull PlayerViewProvider viewProvider) {
        super(viewProvider);
    }

    @Override
    public @NotNull Component formatMessage(@NotNull PlayerChatEvent chatEvent) {
        JoinConfiguration joinConfiguration = JoinConfiguration.separator(Component.space());
        return Component.join(joinConfiguration, Component.text("self"), Component.text(">", NamedTextColor.GRAY),
                super.formatMessage(chatEvent));
    }

    @Override
    protected @NotNull Pair<Audience, ObjectBooleanPair<Component>> getAudience(@NotNull Player player) {
        return Pair.of(player, null);
    }
}
