package org.phantazm.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.GameProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Forwards a player's actual protocol version through game profile properties.
 */
public class ProtocolVersionForwarder {

    /**
     * Handles player logins and adds their protocol version to their profile properties.
     *
     * @param event The triggering {@link LoginEvent}
     */
    @Subscribe
    public void onPlayerLogin(LoginEvent event) {
        Player player = event.getPlayer();
        List<GameProfile.Property> properties = player.getGameProfileProperties();
        List<GameProfile.Property> newProperties = new ArrayList<>(properties);
        int protocol = player.getProtocolVersion().getProtocol();
        newProperties.add(new GameProfile.Property("protocolVersion", String.valueOf(protocol), ""));
        player.setGameProfileProperties(newProperties);
    }

}
