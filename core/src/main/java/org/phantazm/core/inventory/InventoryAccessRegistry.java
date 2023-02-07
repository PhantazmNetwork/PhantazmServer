package org.phantazm.core.inventory;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.player.PlayerView;

/**
 * Keeps track of and switches between {@link InventoryProfile}s.
 */
// TODO: rewrite javadocs
public interface InventoryAccessRegistry {

    /**
     * Checks whether the profile switcher has a current {@link InventoryProfile}.
     *
     * @return Whether the profile switcher has a current {@link InventoryProfile}
     */
    boolean hasCurrentAccess();

    /**
     * Gets the current {@link InventoryProfile} set by the view. This should be checked first with {@link #hasCurrentAccess()}.
     *
     * @return The current view
     * @throws IllegalStateException If no current {@link InventoryProfile} is set
     */
    @NotNull InventoryAccess getCurrentAccess();

    /**
     * Switches the inventory's view to another view based on a {@link Key}.
     *
     * @param key        The {@link Key} of the profile to switch to, or to switch to no profile
     * @param playerView
     * @throws IllegalArgumentException If no {@link InventoryProfile} is registered with the {@link Key}
     */
    void switchAccess(@Nullable Key key, @NotNull PlayerView playerView);

    /**
     * Registers a {@link InventoryProfile} to the view.
     *
     * @param key     The {@link Key} to register the {@link InventoryProfile} with
     * @param profile The {@link InventoryProfile} to register
     * @throws IllegalArgumentException If an {@link InventoryProfile} is already registered with the {@link Key}
     */
    void registerAccess(@NotNull Key key, @NotNull InventoryAccess profile);

    /**
     * Unregisters a {@link InventoryProfile} from the view.
     *
     * @param key The {@link Key} of the {@link InventoryProfile} to unregister
     * @throws IllegalArgumentException If no {@link InventoryProfile} is registered with the {@link Key}
     */
    void unregisterAccess(@NotNull Key key);

}
