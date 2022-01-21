package com.github.phantazmnetwork.api.inventory;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Basic implementation of an {@link InventoryObjectGroupHolder}.
 */
public class BasicInventoryObjectGroupHolder implements InventoryObjectGroupHolder {

    private final IdentityHashMap<InventoryProfile, Map<UUID, InventoryObjectGroup>> groups = new IdentityHashMap<>();

    @Override
    public void registerGroup(@NotNull InventoryProfile profile,
                              @NotNull UUID uuid,
                              @NotNull InventoryObjectGroup group) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(group, "group");

        Map<UUID, InventoryObjectGroup> groupMap = groups.get(profile);
        if (groupMap == null) {
            groupMap = new HashMap<>();
            groups.put(profile, groupMap);

            groupMap.put(uuid, group);
        }
        else if (groupMap.containsKey(uuid)) {
            throw new IllegalArgumentException("Group already registered with uuid");
        }
        else {
            groupMap.put(uuid, group);
        }
    }

    @Override
    public void unregisterGroup(@NotNull InventoryProfile profile, @NotNull UUID uuid) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(uuid, "uuid");

        Map<UUID, InventoryObjectGroup> groupMap = groups.get(profile);
        if (groupMap == null) {
            throw new IllegalArgumentException("No groups registered with profile");
        }

        if (!groupMap.containsKey(uuid)) {
            throw new IllegalArgumentException("No group registered with uuid");
        }

        groupMap.remove(uuid);
        if (groupMap.isEmpty()) {
            groups.remove(profile);
        }
    }

    @Override
    public @NotNull Optional<Map<UUID, InventoryObjectGroup>> getGroups(@NotNull InventoryProfile profile) {
        return Optional.ofNullable(groups.get(profile)).map(Collections::unmodifiableMap);
    }

}
