package com.github.phantazmnetwork.velocity.listener;

import com.github.phantazmnetwork.messaging.packet.PacketSerializer;
import com.github.phantazmnetwork.messaging.proxy.ForwardProtocolVersionPacket;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.ConnectionHandshakeEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ProtocolVersionForwarder {

    private final ChannelIdentifier identifier;

    private final PacketSerializer packetSerializer;

    public ProtocolVersionForwarder(@NotNull ChannelIdentifier identifier, @NotNull PacketSerializer packetSerializer) {
        this.identifier = Objects.requireNonNull(identifier, "identifier");
        this.packetSerializer = Objects.requireNonNull(packetSerializer, "packetSerializer");
    }

    @SuppressWarnings("UnstableApiUsage")
    @Subscribe
    public void onPlayerConnected(ServerPostConnectEvent event) {
        event.getPlayer().getCurrentServer().ifPresent(connection -> {
            Player player = event.getPlayer();
            int protocol = player.getProtocolVersion().getProtocol();

            ForwardProtocolVersionPacket packet = new ForwardProtocolVersionPacket(protocol);
            byte[] bytes = packetSerializer.serializePacket(packet);

            connection.sendPluginMessage(identifier, bytes);
        });
    }

    public void onPlayer(ConnectionHandshakeEvent event) {

    }

}
