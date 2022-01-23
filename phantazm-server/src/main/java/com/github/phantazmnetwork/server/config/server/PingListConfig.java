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
     * The default description string.
     */
    public static final String DEFAULT_DESCRIPTION_STRING = "A Minecraft Server";

    /**
     * The default PingListConfig instance.
     */
    public static final PingListConfig DEFAULT = new PingListConfig(Component.text(DEFAULT_DESCRIPTION_STRING));

    /**
     * Creates config for the server's ping list.
     * @param description The MOTD set in the server list
     */
    public PingListConfig {
        Objects.requireNonNull(description, "description");
    }

}
