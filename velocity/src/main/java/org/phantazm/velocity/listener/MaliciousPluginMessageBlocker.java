package org.phantazm.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

/**
 * Blocks malicious plugin messages from players that go through the plugin's proxy channel.
 */
public class MaliciousPluginMessageBlocker {

    private final Set<? extends ChannelIdentifier> blockedIdentifiers;

    public MaliciousPluginMessageBlocker(@NotNull Set<? extends ChannelIdentifier> blockedIdentifiers) {
        this.blockedIdentifiers = Objects.requireNonNull(blockedIdentifiers);
    }

    /**
     * Handles plugin messages from players and blocks messages that go through the proxy channel.
     *
     * @param event The triggering {@link PluginMessageEvent}
     */
    @Subscribe
    public void onPlayerMessage(PluginMessageEvent event) {
        if (blockedIdentifiers.contains(event.getIdentifier()) && event.getSource() instanceof Player player) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            player.disconnect(Component.text("Malicious plugin message.", NamedTextColor.RED));
        }
    }

}
