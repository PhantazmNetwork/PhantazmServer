package org.phantazm.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.phantazm.commons.Namespaces;
import org.phantazm.messaging.MessageChannels;
import org.phantazm.velocity.listener.MaliciousPluginMessageBlocker;
import org.phantazm.velocity.listener.ProtocolVersionForwarder;

/**
 * A velocity plugin used to communicate information to the Phantazm server.
 */
@Plugin(id = "phantazm", name = "Phantazm", version = "1.0-SNAPSHOT",
        description = "A velocity plugin used to communicate information to the Phantazm server.", authors = {"thamid"})
public class PhantazmPlugin {

    private final ProxyServer server;

    /**
     * Creates a {@link PhantazmPlugin}.
     *
     * @param server The {@link ProxyServer} the plugin is running on
     */
    @Inject
    public PhantazmPlugin(ProxyServer server) {
        this.server = server;
    }

    /**
     * Registers necessary plugin objects on proxy startup.
     *
     * @param event The triggering {@link ProxyInitializeEvent}
     */
    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        ChannelIdentifier identifier =
                MinecraftChannelIdentifier.create(Namespaces.PHANTAZM, MessageChannels.PROXY_TO_SERVER);
        server.getChannelRegistrar().register(identifier);
        server.getEventManager().register(this, new MaliciousPluginMessageBlocker(identifier));

        server.getEventManager().register(this, new ProtocolVersionForwarder());
    }

}
