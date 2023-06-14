package org.phantazm.velocity.listener;

import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.packet.Packet;
import org.phantazm.messaging.packet.PacketHandler;
import org.phantazm.messaging.packet.c2p.MapDataVersionQueryPacket;
import org.phantazm.messaging.packet.c2p.MapDataVersionResponsePacket;
import org.phantazm.messaging.serialization.PacketSerializer;
import org.phantazm.messaging.serialization.PacketSerializers;
import org.phantazm.velocity.packet.ByteArrayInputDataReader;
import org.phantazm.velocity.packet.ByteArrayOutputDataWriter;
import org.phantazm.zombies.map.MapSettingsInfo;

import java.util.Map;

/**
 * Handles all messaging that involves the proxy
 */
public class ProxyMessagingHandler {

    private final Map<ChannelIdentifier, PacketHandler<Player>> packetHandlers;

    /**
     * Creates a new {@link ProxyMessagingHandler}.
     * @param clientToProxyIdentifier The identifier for the channel between the client and the proxy
     */
    @SuppressWarnings("UnstableApiUsage")
    public ProxyMessagingHandler(@NotNull ChannelIdentifier clientToProxyIdentifier) {
        PacketSerializer clientToProxy = PacketSerializers.clientToProxySerializer(
                () -> new ByteArrayOutputDataWriter(ByteStreams.newDataOutput()),
                data -> new ByteArrayInputDataReader(ByteStreams.newDataInput(data)));
        packetHandlers = Map.of(clientToProxyIdentifier, new PacketHandler<>(clientToProxy) {
            @Override
            protected void handlePacket(@NotNull Player player, @NotNull Packet packet) {
                if (packet instanceof MapDataVersionQueryPacket) {
                    output(player, new MapDataVersionResponsePacket(MapSettingsInfo.MAP_DATA_VERSION));
                }
            }

            @Override
            protected void sendToReceiver(@NotNull Player player, byte @NotNull [] data) {
                player.sendPluginMessage(clientToProxyIdentifier, data);
            }
        });
    }

    /**
     * Handles a {@link PluginMessageEvent}.
     * @param event The event
     */
    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!(event.getSource() instanceof Player player)) {
            return;
        }

        PacketHandler<Player> packetHandler = packetHandlers.get(event.getIdentifier());
        if (packetHandler != null) {
            packetHandler.handleData(player, event.getData());
            event.setResult(PluginMessageEvent.ForwardResult.handled());
        }
    }

}
