package org.phantazm.server;

import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import net.kyori.adventure.text.Component;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.server.permission.PermissionHandler;
import org.phantazm.server.player.LoginValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ValidationFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationFeature.class);

    private ValidationFeature() {
    }

    static void initialize(@NotNull EventNode<Event> rootNode, @NotNull LoginValidator validator,
            @NotNull PermissionHandler permissionHandler) {
        rootNode.addListener(AsyncPlayerPreLoginEvent.class, event -> {
            try {
                UUID uuid = event.getPlayerUuid();
                BooleanObjectPair<Component> validationResult = validator.validateLogin(uuid);
                if (!validationResult.firstBoolean()) {
                    event.getPlayer().kick(validationResult.second());
                }

                permissionHandler.applyPermissions(uuid, event.getPlayer());
            }
            catch (Throwable e) {
                LOGGER.warn("Error during AsyncPlayerPreLoginEvent", e);
            }
        });
    }
}
