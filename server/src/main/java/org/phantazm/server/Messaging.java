package org.phantazm.server;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;
import org.phantazm.messaging.MessageChannels;
import org.phantazm.messaging.packet.Packet;
import org.phantazm.messaging.packet.PacketHandler;
import org.phantazm.messaging.packet.c2s.MapDataVersionQueryPacket;
import org.phantazm.messaging.packet.c2s.MapDataVersionResponsePacket;
import org.phantazm.messaging.serialization.PacketSerializer;
import org.phantazm.messaging.serialization.PacketSerializers;
import org.phantazm.server.config.server.AuthType;
import org.phantazm.server.packet.BinaryDataReader;
import org.phantazm.server.packet.BinaryDataWriter;
import org.phantazm.zombies.map.MapSettingsInfo;

import java.util.Map;

/**
 * Main entrypoint for plugin messaging. This is used to communicate with the proxy and players.
 */
public final class Messaging {

    private Messaging() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull EventNode<Event> global, @NotNull AuthType authType) {
        PacketSerializer clientToServer =
                PacketSerializers.clientToServerSerializer(() -> new BinaryDataWriter(new BinaryWriter()),
                        data -> new BinaryDataReader(new BinaryReader(data)));
        Key clientToServerIdentifier = Key.key(Namespaces.PHANTAZM, MessageChannels.CLIENT_TO_SERVER);
        Map<String, PacketHandler<Player>> packetHandlers =
                Map.of(MessageChannels.CLIENT_TO_SERVER, new PacketHandler<>(clientToServer) {
                    @Override
                    protected void handlePacket(@NotNull Player player, @NotNull Packet packet) {
                        if (packet instanceof MapDataVersionQueryPacket) {
                            output(player, new MapDataVersionResponsePacket(MapSettingsInfo.MAP_DATA_VERSION));
                        }
                    }

                    @Override
                    protected void sendToReceiver(@NotNull Player player, byte @NotNull [] data) {
                        player.sendPluginMessage(clientToServerIdentifier.toString(), data);
                    }
                });

        global.addListener(PlayerPluginMessageEvent.class, event -> {
            String identifier = event.getIdentifier();
            if (!identifier.contains(":")) {
                return;
            }

            String[] split = identifier.split(":");
            if (split.length != 2 || !split[0].equals(Namespaces.PHANTAZM)) {
                return;
            }

            PacketHandler<Player> packetHandler = packetHandlers.get(split[1]);
            if (packetHandler == null) {
                return;
            }

            packetHandler.handleData(event.getPlayer(), event.getMessage());
        });
    }

}
