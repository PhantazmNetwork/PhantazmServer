package com.github.phantazmnetwork.zombies.equipment.gun.reload.actionbar;

import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Chooses a {@link Component} to send to an {@link Audience} based on the gun's relaod progress.
 */
@FunctionalInterface
public interface ReloadActionBarChooser {

    /**
     * Chooses a {@link Component} to send to an {@link Audience} based on the gun's reload progress.
     *
     * @param state The state of the gun
     * @param entity The {@link Entity} that is reloading the gun
     * @param progress The gun's reload progress
     * @return The {@link Component} to send to the {@link Audience}
     */
    @NotNull Component choose(@NotNull GunState state, @NotNull Entity entity, float progress);

}
