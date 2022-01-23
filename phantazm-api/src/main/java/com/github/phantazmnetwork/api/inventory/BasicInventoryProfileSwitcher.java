package com.github.phantazmnetwork.api.inventory;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Basic implementation of an {@link InventoryProfileSwitcher}.
 */
public class BasicInventoryProfileSwitcher implements InventoryProfileSwitcher {

    private final Map<Key, InventoryProfile> profileMap = new HashMap<>();

    private InventoryProfile currentProfile = null;

    @Override
    public boolean hasCurrentProfile() {
        return currentProfile != null;
    }

    @Override
    public @NotNull InventoryProfile getCurrentProfile() {
        if (!hasCurrentProfile()) {
            throw new IllegalStateException("No inventory profile set");
        }

        return currentProfile;
    }

    @Override
    public void switchProfile(@Nullable Key key) {
        if (key == null) {
            currentProfile = null;
        }
        else {
            InventoryProfile profile = profileMap.get(key);
            if (profile == null) {
                throw new IllegalArgumentException("No matching inventory profile found");
            }

            currentProfile = profileMap.get(key);
        }
    }

    @Override
    public void registerProfile(@NotNull Key key, @NotNull InventoryProfile profile) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(profile, "profile");

        if (profileMap.containsKey(key)) {
            throw new IllegalArgumentException("Inventory profile already registered");
        }

        profileMap.put(key, profile);
    }

    @Override
    public void unregisterProfile(@NotNull Key key) {
        Objects.requireNonNull(key, "uuid");

        if (!profileMap.containsKey(key)) {
            throw new IllegalArgumentException("Inventory profile not yet registered");
        }

        profileMap.remove(key);
    }

}
