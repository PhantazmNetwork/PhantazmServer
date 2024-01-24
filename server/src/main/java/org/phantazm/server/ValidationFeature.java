package org.phantazm.server;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.server.context.PlayerContext;
import org.phantazm.server.permission.PermissionHandler;
import org.phantazm.server.validator.LoginValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

public class ValidationFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationFeature.class);

    private ValidationFeature() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull PlayerContext playerContext) {
        LoginValidator validator = playerContext.loginValiator();
        PermissionHandler permissionHandler = playerContext.permissionHandler();

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerPreLoginEvent.class, event -> {
            try {
                UUID uuid = event.getPlayerUuid();
                LoginValidator.LoginEntry validationResult = validator.login(uuid).get();
                if (!validationResult.canJoin()) {
                    event.getPlayer().kick(Objects.requireNonNullElseGet(validationResult.banReason(), Component::empty));
                    return;
                }

                permissionHandler.applyPermissions(event.getPlayer());
            } catch (Throwable e) {
                LOGGER.warn("Error during AsyncPlayerPreLoginEvent", e);
            }
        });
    }
}
