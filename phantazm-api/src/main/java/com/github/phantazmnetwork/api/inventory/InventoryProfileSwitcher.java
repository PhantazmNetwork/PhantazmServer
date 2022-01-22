package com.github.phantazmnetwork.api.inventory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Keeps track of and switches between {@link InventoryProfile}s.
 */
public interface InventoryProfileSwitcher {

    /**
     * Checks whether the profile switcher has a current {@link InventoryProfile}.
     * @return Whether the profile switcher has a current {@link InventoryProfile}
     */
    default boolean hasCurrentProfile() {
        return getCurrentProfile() != null;
    }

    /**
     * Gets the current {@link InventoryProfile} set by the view, or null if one is not set.
     * @return The current view
     */
    InventoryProfile getCurrentProfile();

    /**
     * Switches the inventory's view to another view based on a {@link UUID}.
     * @param uuid The {@link UUID} of the profile to switch to, or to switch to no profile
     * @throws IllegalArgumentException If no {@link InventoryProfile} is registered with the {@link UUID}
     */
    void switchProfile(@Nullable UUID uuid);

    /**
     * Registers a {@link InventoryProfile} to the view.
     * @param uuid The {@link UUID} to register the {@link InventoryProfile} with
     * @param profile The {@link InventoryProfile} to register
     * @throws IllegalArgumentException If an {@link InventoryProfile} is already registered with the {@link UUID}
     */
    void registerProfile(@NotNull UUID uuid, @NotNull InventoryProfile profile);

    /**
     * Unregisters a {@link InventoryProfile} from the view.
     * @param uuid The {@link UUID} of the {@link InventoryProfile} to unregister
     * @throws IllegalArgumentException If no {@link InventoryProfile} is registered with the {@link UUID}
     */
    void unregisterProfile(@NotNull UUID uuid);

}
