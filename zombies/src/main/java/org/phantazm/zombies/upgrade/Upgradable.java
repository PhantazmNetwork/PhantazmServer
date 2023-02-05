package org.phantazm.zombies.upgrade;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

/**
 * Represents an item that can be upgraded.
 */
public interface Upgradable extends Keyed {

    /**
     * Gets a {@link Set} of {@link Key}s that represent logical next upgrades that the item suggests.
     *
     * @return A {@link Set} of {@link Key}s that represent logical next upgrades that the item suggests
     */
    @Unmodifiable @NotNull Set<Key> getSuggestedUpgrades();

    /**
     * Gets a {@link Set} of {@link Key}s of all possible item levels.
     *
     * @return A {@link Set} of {@link Key}s of all possible item levels
     */
    @Unmodifiable @NotNull Set<Key> getLevels();

    /**
     * Sets the item's level.
     *
     * @param key The {@link Key} of the level to set
     */
    void setLevel(@NotNull Key key);

    /**
     * Gets the current level of this Upgradable object.
     *
     * @return the current level key
     */
    @NotNull Key currentLevel();
}
