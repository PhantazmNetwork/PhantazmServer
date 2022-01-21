package com.github.phantazmnetwork.api.inventory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Basic implementation of an {@link InventoryProfileSwitcher}.
 */
public class BasicInventoryProfileSwitcher implements InventoryProfileSwitcher {

    private final Map<UUID, InventoryProfile> profileMap = new HashMap<>();

    private InventoryProfile currentProfile = null;

    @Override
    public @Nullable InventoryProfile getCurrentProfile() {
        return currentProfile;
    }

    @Override
    public void switchProfile(@Nullable UUID uuid) {
        if (uuid == null) {
            hideCurrentProfile();
            currentProfile = null;
        }
        else {
            InventoryProfile profile = profileMap.get(uuid);
            if (profile == null) {
                throw new IllegalArgumentException("No matching inventory profile found");
            }

            hideCurrentProfile();

            currentProfile = profileMap.get(uuid);
            currentProfile.setVisible(true);
        }
    }

    /**
     * Hides the current profile if one is set.
     */
    private void hideCurrentProfile() {
        if (currentProfile != null) {
            currentProfile.setVisible(false);
        }
    }

    @Override
    public void registerProfile(@NotNull UUID uuid, @NotNull InventoryProfile profile) {
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(profile, "profile");

        if (profileMap.containsKey(uuid)) {
            throw new IllegalArgumentException("Inventory profile already registered");
        }

        profileMap.put(uuid, profile);
    }

    @Override
    public void unregisterProfile(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");

        if (!profileMap.containsKey(uuid)) {
            throw new IllegalArgumentException("Inventory profile not yet registered");
        }

        profileMap.remove(uuid);
    }

}
