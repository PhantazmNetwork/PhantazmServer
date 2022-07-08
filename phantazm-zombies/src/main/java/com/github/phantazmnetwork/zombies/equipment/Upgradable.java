package com.github.phantazmnetwork.zombies.equipment;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents an item that can be upgraded.
 */
public interface Upgradable {

    /**
     * Gets a {@link Set} of {@link Key}s that represent logical next upgrades that the item suggests.
     * @return A {@link Set} of {@link Key}s that represent logical next upgrades that the item suggests
     */
    @NotNull Set<Key> getSuggestedUpgrades();

    /**
     * Gets a {@link Set} of {@link Key}s of all possible item levels.
     * @return A {@link Set} of {@link Key}s of all possible item levels
     */
    @NotNull Set<Key> getLevels();

    /**
     * Sets the item's level.
     * @param key The {@link Key} of the level to set
     */
    void setLevel(@NotNull Key key);

}
