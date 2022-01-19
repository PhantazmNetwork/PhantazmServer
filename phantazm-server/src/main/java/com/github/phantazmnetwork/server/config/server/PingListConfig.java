package com.github.phantazmnetwork.server.config.server;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Config for the server's ping list.
 * @param description The MOTD set in the server list
 */
public record PingListConfig(@NotNull Component description) {

}
