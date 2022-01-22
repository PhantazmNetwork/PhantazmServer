package com.github.phantazmnetwork.api.inventory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.UUID;

/**
 * A container for {@link InventoryObjectGroup}s which associates {@link InventoryProfile}s with {@link InventoryObjectGroup}s.
 */
public interface InventoryObjectGroupHolder {

    /**
     * Registers a {@link InventoryObjectGroup} to the group holder.
     * @param profile The {@link InventoryProfile} to register the {@link InventoryObjectGroup} to
     * @param uuid The {@link UUID} to register the {@link InventoryObjectGroup} with
     * @param group The {@link InventoryObjectGroup} to register
     * @throws IllegalArgumentException If an {@link InventoryObjectGroup} is already registered with the given {@link UUID}
     */
    void registerGroup(@NotNull InventoryProfile profile, @NotNull UUID uuid, @NotNull InventoryObjectGroup group);

    /**
     * Unregisters a {@link InventoryObjectGroup} from the group holder.
     * @param profile The {@link InventoryProfile} to unregister the {@link InventoryObjectGroup} from
     * @param uuid The {@link UUID} of the {@link InventoryObjectGroup} to unregister
     * @throws IllegalArgumentException If no {@link InventoryObjectGroup}s are registered with the given {@link InventoryProfile}
     * @throws IllegalArgumentException If no {@link InventoryObjectGroup}s are registered with the given {@link UUID}
     */
    void unregisterGroup(@NotNull InventoryProfile profile, @NotNull UUID uuid);

    /**
     * Checks whether the group holder has a {@link InventoryProfile}.
     * @param profile The {@link InventoryProfile} to check
     * @return Whether the group holder has the {@link InventoryProfile}
     */
    default boolean hasGroup(@NotNull InventoryProfile profile) {
        return getGroups(profile) != null;
    }

    /**
     * Gets an unmodifiable view of the {@link InventoryObjectGroup}s registered with a {@link InventoryProfile}.
     * @param profile The {@link InventoryProfile} to get the {@link InventoryObjectGroup}s of
     * @return An unmodifiable view of the {@link InventoryObjectGroup}, or null if no such groups exist
     */
    @UnmodifiableView Map<UUID, InventoryObjectGroup> getGroups(@NotNull InventoryProfile profile);

}
