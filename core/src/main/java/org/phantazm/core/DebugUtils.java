package org.phantazm.core;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public final class DebugUtils {
    private DebugUtils() {
    }

    public static void debugMessage(@NotNull String message) {
        MinecraftServer.getInstanceManager().getInstances()
            .forEach(instance -> instance.sendMessage(Component.text(message)));
    }
}
