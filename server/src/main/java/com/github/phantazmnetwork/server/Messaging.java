package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.core.player.BasicPlayerViewProvider;
import com.github.phantazmnetwork.messaging.MessageChannels;
import com.github.phantazmnetwork.server.config.server.AuthType;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Main entrypoint for plugin messaging. This is used to communicate with the proxy and players.
 */
public final class Messaging {

    private Messaging() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull EventNode<Event> global, @NotNull BasicPlayerViewProvider basicPlayerViewProvider,
            @NotNull AuthType authType) {
        if (authType == AuthType.BUNGEE || authType == AuthType.VELOCITY) {
            global.addListener(PlayerPluginMessageEvent.class, event -> {
                String identifier = event.getIdentifier();
                if (!identifier.contains(":")) {
                    return;
                }

                String[] split = identifier.split(":");
                if (split.length != 2 || !split[0].equals(Namespaces.PHANTAZM)) {
                    return;
                }

                if (split[1].equals(MessageChannels.PROXY)) {
                    // proxy message handling
                }
            });
        }
    }

}
