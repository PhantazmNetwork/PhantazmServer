package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Defines the positional data for a shop.
 */
public record ShopInfo(@NotNull Key id,
                       @NotNull Vec3I triggerLocation,
                       @NotNull Evaluation predicateEvaluation,
                       @NotNull ConfigNode data) {
    /**
     * Creates a new instance of this record.
     *
     * @param id              the type of shop
     * @param triggerLocation where the trigger location is (the block or hologram that activates this shop)
     */
    public ShopInfo {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(triggerLocation, "triggerLocation");
        Objects.requireNonNull(predicateEvaluation, "predicateEvaluation");
        Objects.requireNonNull(data, "data");
    }
}
