package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.core.player.BasicPlayerViewProvider;
import com.github.phantazmnetwork.messaging.MessageChannels;
import com.github.phantazmnetwork.messaging.packet.PacketSerializer;
import com.github.phantazmnetwork.messaging.packet.PacketSerializers;
import com.github.phantazmnetwork.messaging.proxy.ForwardProtocolVersionPacket;
import com.github.phantazmnetwork.server.config.server.AuthType;
import com.github.phantazmnetwork.server.packet.BinaryDataReader;
import com.github.phantazmnetwork.server.packet.BinaryDataWriter;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public final class Messaging {

    private Messaging() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull EventNode<Event> global, @NotNull BasicPlayerViewProvider basicPlayerViewProvider,
                           @NotNull AuthType authType) {
        if (authType == AuthType.BUNGEE || authType == AuthType.VELOCITY) {
            PacketSerializer proxyPacketSerializer =
                    PacketSerializers.createProxySerializer(() -> new BinaryDataWriter(new BinaryWriter()),
                                                            bytes -> new BinaryDataReader(new BinaryReader(bytes))
                    );

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
                    proxyPacketSerializer.deserializePacket(event.getMessage()).ifPresent(packet -> {
                        if (packet instanceof ForwardProtocolVersionPacket protocolPacket) {
                            basicPlayerViewProvider.updateProtocolVersion(event.getPlayer(),
                                                                          protocolPacket.protocolVersion()
                            );
                        }
                    });
                }
            });
        }
        else {
            global.addListener(PlayerLoginEvent.class, event -> {
                basicPlayerViewProvider.updateProtocolVersion(event.getPlayer(), event.getPlayer().getPlayerConnection()
                                                                                      .getProtocolVersion());
            });
        }

        global.addListener(PlayerDisconnectEvent.class, event -> {
            basicPlayerViewProvider.invalidatePlayerInfo(event.getPlayer());
        });
    }

}
