package org.phantazm.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Blocks malicious plugin messages from players that go through the plugin's proxy channel.
 */
public class MaliciousPluginMessageBlocker {

    private final ChannelIdentifier proxyIdentifier;

    /**
     * Creates a {@link MaliciousPluginMessageBlocker}.
     *
     * @param proxyIdentifier The {@link ChannelIdentifier} for the plugin's proxy channel
     */
    public MaliciousPluginMessageBlocker(@NotNull ChannelIdentifier proxyIdentifier) {
        this.proxyIdentifier = Objects.requireNonNull(proxyIdentifier, "proxyIdentifier");
    }

    /**
     * Handles plugin messages from players and blocks messages that go through the proxy channel.
     *
     * @param event The triggering {@link PluginMessageEvent}
     */
    @Subscribe
    public void onPlayerMessage(PluginMessageEvent event) {
        if (event.getIdentifier().getId().equals(proxyIdentifier.getId()) &&
                event.getSource() instanceof Player player) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            player.disconnect(Component.text("Malicious plugin message.", NamedTextColor.RED));
        }
    }

}
