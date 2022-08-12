package com.github.phantazmnetwork.core.inventory;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Basic implementation of an {@link InventoryAccessRegistry}.
 */
public class BasicInventoryAccessRegistry implements InventoryAccessRegistry {

    private final Map<Key, InventoryAccess> accessMap = new HashMap<>();

    private InventoryAccess currentAccess = null;

    @Override
    public boolean hasCurrentAccess() {
        return currentAccess != null;
    }

    @Override
    public @NotNull InventoryAccess getCurrentAccess() {
        if (!hasCurrentAccess()) {
            throw new IllegalStateException("No inventory profile set");
        }

        return currentAccess;
    }

    @Override
    public void switchAccess(@Nullable Key key) {
        if (key == null) {
            currentAccess = null;
        }
        else {
            InventoryAccess access = accessMap.get(key);
            if (access == null) {
                throw new IllegalArgumentException("No matching inventory access found");
            }

            currentAccess = accessMap.get(key);
        }
    }

    @Override
    public void registerAccess(@NotNull Key key, @NotNull InventoryAccess profile) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(profile, "profile");

        if (accessMap.containsKey(key)) {
            throw new IllegalArgumentException("Inventory profile already registered");
        }

        accessMap.put(key, profile);
    }

    @Override
    public void unregisterAccess(@NotNull Key key) {
        Objects.requireNonNull(key, "key");

        if (!accessMap.containsKey(key)) {
            throw new IllegalArgumentException("Inventory profile not yet registered");
        }

        accessMap.remove(key);
    }

}
