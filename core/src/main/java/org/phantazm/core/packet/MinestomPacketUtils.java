package org.phantazm.core.packet;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.packet.Packet;

public class MinestomPacketUtils {

    public static byte @NotNull [] serialize(@NotNull Packet packet) {
        return NetworkBuffer.makeArray(buffer -> {
            packet.write(BinaryDataWriter.fromNetworkBuffer(buffer));
        });
    }

}
