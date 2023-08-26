package org.phantazm.zombies.map;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.vector.Bounds3I;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Defines the positional data for a shop.
 */
public record ShopInfo(
    @NotNull Key id,
    @NotNull Bounds3I trigger,
    @NotNull Evaluation predicateEvaluation,
    @NotNull ConfigNode data) {
    /**
     * Creates a new instance of this record.
     *
     * @param id      the type of shop
     * @param trigger the bounds of the trigger location
     */
    public ShopInfo {
        Objects.requireNonNull(id);
        Objects.requireNonNull(trigger);
        Objects.requireNonNull(predicateEvaluation);
        Objects.requireNonNull(data);
    }
}
