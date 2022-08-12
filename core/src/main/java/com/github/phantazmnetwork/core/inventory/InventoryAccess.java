package com.github.phantazmnetwork.core.inventory;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.Objects;

public record InventoryAccess(@NotNull InventoryProfile profile,
                              @NotNull @Unmodifiable Map<Key, InventoryObjectGroup> groups) {

    public InventoryAccess(@NotNull InventoryProfile profile, @NotNull Map<Key, InventoryObjectGroup> groups) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.groups = Map.copyOf(groups);
    }


}
