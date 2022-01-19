package com.github.phantazmnetwork.api.world;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import org.jetbrains.annotations.NotNull;

/**
 * Loads usable {@link Instance}s
 */
public interface WorldLoader {

    /**
     * Loads an {@link Instance}
     * @param instanceManager The {@link InstanceManager} to register the world with
     * @param worldName The name of the world
     * @return A new {@link Instance}
     */
    @NotNull Instance loadWorld(@NotNull InstanceManager instanceManager, @NotNull String worldName);

}
