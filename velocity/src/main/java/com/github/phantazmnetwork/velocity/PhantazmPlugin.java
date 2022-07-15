package com.github.phantazmnetwork.velocity;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.messaging.MessageChannels;
import com.github.phantazmnetwork.messaging.packet.PacketSerializer;
import com.github.phantazmnetwork.messaging.packet.PacketSerializers;
import com.github.phantazmnetwork.velocity.listener.MaliciousPluginMessageBlocker;
import com.github.phantazmnetwork.velocity.listener.ProtocolVersionForwarder;
import com.github.phantazmnetwork.velocity.packet.ByteArrayInputDataReader;
import com.github.phantazmnetwork.velocity.packet.ByteArrayOutputDataWriter;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

@Plugin(id = "phantazm", name = "Phantazm", version = "1.0-SNAPSHOT", authors = {"thamid"})
public class PhantazmPlugin {

    private final ProxyServer server;

    @Inject
    public PhantazmPlugin(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        ChannelIdentifier identifier = MinecraftChannelIdentifier.create(Namespaces.PHANTAZM, MessageChannels.PROXY);
        server.getChannelRegistrar().register(identifier);
        server.getEventManager().register(this, new MaliciousPluginMessageBlocker(identifier));

        @SuppressWarnings("UnstableApiUsage")
        PacketSerializer proxySerializer = PacketSerializers.createProxySerializer(
                () -> new ByteArrayOutputDataWriter(ByteStreams.newDataOutput()),
                bytes -> new ByteArrayInputDataReader(ByteStreams.newDataInput(bytes))
        );
        server.getEventManager().register(this, new ProtocolVersionForwarder(identifier, proxySerializer));
    }

}
