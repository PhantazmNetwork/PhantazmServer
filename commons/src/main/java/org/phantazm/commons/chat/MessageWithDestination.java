package org.phantazm.commons.chat;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record MessageWithDestination(@NotNull Component component,
    @NotNull ChatDestination destination) {

}
