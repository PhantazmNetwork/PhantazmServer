package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Defines a shop.
 */
public record ShopInfo(@NotNull Key id,
                       @NotNull Vec3I triggerLocation,
                       int cost) {
    /**
     * Creates a new instance of this record.
     * @param id the type of shop
     * @param triggerLocation where the trigger location is (the block or hologram that activates this shop)
     * @param cost the cost of the shop
     */
    public ShopInfo {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(triggerLocation, "triggerLocation");
    }

    /**
     * Creates a new instance of this record, with a default cost value of 0.
     * @param id the type of shop
     * @param triggerLocation where the trigger location is (the block or hologram that activates this shop)
     */
    public ShopInfo(@NotNull Key id, @NotNull Vec3I triggerLocation) {
        this(id, triggerLocation, 0);
    }
}
