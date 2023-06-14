package org.phantazm.server;

import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import net.kyori.adventure.text.Component;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.server.player.LoginValidator;

public class ValidationFeature {
    static void initialize(@NotNull EventNode<Event> rootNode, @NotNull LoginValidator validator) {
        rootNode.addListener(AsyncPlayerPreLoginEvent.class, event -> {
            BooleanObjectPair<Component> validationResult = validator.validateLogin(event.getPlayerUuid());
            if (!validationResult.firstBoolean()) {
                event.getPlayer().kick(validationResult.second());
            }
        });
    }
}
