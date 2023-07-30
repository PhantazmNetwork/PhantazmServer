package org.phantazm.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.packet.player.MapDataVersionQueryPacket;
import org.phantazm.messaging.packet.proxy.MapDataVersionResponsePacket;
import org.phantazm.velocity.packet.VelocityPacketUtils;
import org.phantazm.zombies.map.MapSettingsInfo;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Handles all messaging that involves the proxy
 */
public class ProxyMessagingHandler {

    private final Map<ChannelIdentifier, BiConsumer<Player, byte[]>> packetHandlers;

    /**
     * Creates a new {@link ProxyMessagingHandler}.
     */
    public ProxyMessagingHandler(@NotNull Map<ChannelIdentifier, BiConsumer<Player, byte[]>> packetHandlers) {
        this.packetHandlers = Objects.requireNonNull(packetHandlers, "packetHandlers");
    }

    public static @NotNull ProxyMessagingHandler createDefault() {
        Map<ChannelIdentifier, BiConsumer<Player, byte[]>> packetHandlers = Map.of(MinecraftChannelIdentifier.from(MapDataVersionQueryPacket.ID), (player, data) -> {
            VelocityPacketUtils.sendPacket(player, new MapDataVersionResponsePacket(MapSettingsInfo.MAP_DATA_VERSION));
        });

        return new ProxyMessagingHandler(packetHandlers);
    }

    /**
     * Handles a {@link PluginMessageEvent}.
     *
     * @param event The event
     */
    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!(event.getSource() instanceof Player player)) {
            return;
        }

        BiConsumer<Player, byte[]> packetHandler = packetHandlers.get(event.getIdentifier());
        if (packetHandler != null) {
            packetHandler.accept(player, event.getData());
            event.setResult(PluginMessageEvent.ForwardResult.handled());
        }
    }

}
