package com.github.phantazmnetwork.api.inventory;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Keeps track of and switches between {@link InventoryProfile}s.
 */
public interface InventoryProfileSwitcher {

    /**
     * Checks whether the profile switcher has a current {@link InventoryProfile}.
     *
     * @return Whether the profile switcher has a current {@link InventoryProfile}
     */
    boolean hasCurrentProfile();

    /**
     * Gets the current {@link InventoryProfile} set by the view. This should be checked first with {@link #hasCurrentProfile()}.
     *
     * @return The current view
     * @throws IllegalStateException If no current {@link InventoryProfile} is set
     */
    @NotNull InventoryProfile getCurrentProfile();

    /**
     * Switches the inventory's view to another view based on a {@link Key}.
     *
     * @param key The {@link Key} of the profile to switch to, or to switch to no profile
     * @throws IllegalArgumentException If no {@link InventoryProfile} is registered with the {@link Key}
     */
    void switchProfile(@Nullable Key key);

    /**
     * Registers a {@link InventoryProfile} to the view.
     *
     * @param key     The {@link Key} to register the {@link InventoryProfile} with
     * @param profile The {@link InventoryProfile} to register
     * @throws IllegalArgumentException If an {@link InventoryProfile} is already registered with the {@link Key}
     */
    void registerProfile(@NotNull Key key, @NotNull InventoryProfile profile);

    /**
     * Unregisters a {@link InventoryProfile} from the view.
     *
     * @param key The {@link Key} of the {@link InventoryProfile} to unregister
     * @throws IllegalArgumentException If no {@link InventoryProfile} is registered with the {@link Key}
     */
    void unregisterProfile(@NotNull Key key);

}
