package com.github.phantazmnetwork.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MaliciousPluginMessageBlocker {

    private final ChannelIdentifier proxyIdentifier;

    public MaliciousPluginMessageBlocker(@NotNull ChannelIdentifier proxyIdentifier) {
        this.proxyIdentifier = Objects.requireNonNull(proxyIdentifier, "proxyIdentifier");
    }

    @Subscribe
    public void onPlayerMessage(PluginMessageEvent event) {
        if (event.getIdentifier().getId().equals(proxyIdentifier.getId()) &&
            event.getSource() instanceof Player player) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            player.disconnect(Component.text("Malicious plugin message.", NamedTextColor.RED));
        }
    }

}
