package com.github.phantazmnetwork.server.config.server;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Authentication type to verify players.
 */
public enum AuthType {

    /**
     * No authentication should be used
     */
    NONE,

    /**
     * Mojang's default authentication
     */
    MOJANG,

    /**
     * Bungeecord or velocity with bungeeguard authentication
     */
    BUNGEE,

    /**
     * Velocity modern authentication
     */
    VELOCITY;

    private static final Map<String, AuthType> BY_NAME;

    static {
        Map<String, AuthType> byName = new HashMap<>();
        for (AuthType authType : AuthType.values()) {
            byName.put(authType.name(), authType);
        }

        BY_NAME = Collections.unmodifiableMap(byName);
    }

    /**
     * Gets an {@link AuthType} by its name
     * @param name The name of the {@link AuthType}
     * @return An {@link Optional} of the {@link AuthType} associated with the name
     */
    public static @NotNull Optional<AuthType> getByName(@NotNull String name) {
        Objects.requireNonNull(name, "name");
        return Optional.ofNullable(BY_NAME.get(name));
    }

}
