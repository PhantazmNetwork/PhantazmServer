package com.github.phantazmnetwork.server.config.server;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Config for the server's ping list.
 * @param description The MOTD set in the server list
 */
public record PingListConfig(@NotNull Component description) {

    /**
     * Creates config for the server's ping list.
     * @param description The MOTD set in the server list
     */
    public PingListConfig {
        Objects.requireNonNull(description, "description");
    }

}
