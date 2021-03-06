package com.github.phantazmnetwork.api.chat;

import com.github.phantazmnetwork.api.player.PlayerViewProvider;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A {@link ChatChannel} that sends a message to an entire {@link Instance}.
 */
public class InstanceChatChannel extends BasicChatChannel {

    /**
     * Creates a {@link InstanceChatChannel}.
     * @param viewProvider The {@link InstanceChatChannel}'s {@link PlayerViewProvider}
     */
    public InstanceChatChannel(@NotNull PlayerViewProvider viewProvider) {
        super(viewProvider);
    }

    @Override
    protected @NotNull Pair<Audience, Component> getAudience(@NotNull Player player) {
        Instance instance = player.getInstance();
        if (instance == null) {
            return Pair.of(null, Component.text("You are not in an instance.", NamedTextColor.RED));
        }

        return Pair.of((ForwardingAudience) () -> List.of(instance), null);
    }

}
