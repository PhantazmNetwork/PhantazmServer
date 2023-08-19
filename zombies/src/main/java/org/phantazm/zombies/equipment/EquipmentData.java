package org.phantazm.zombies.equipment;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * General data for some levelled equipment.
 *
 * @param type      The type of the equipment, see {@link EquipmentTypes}
 * @param name      The unique {@link Key} name of the equipment
 * @param rootLevel The initial level of the equipment
 */
public record EquipmentData(@NotNull Key type, @NotNull Key name, @NotNull Key rootLevel) {
    /**
     * Creates a {@link EquipmentData}.
     *
     * @param type      The type of the equipment
     * @param name      The unique {@link Key} name of the equipment
     * @param rootLevel The initial level of the equipment
     */
    public EquipmentData {
        Objects.requireNonNull(type);
        Objects.requireNonNull(name);
        Objects.requireNonNull(rootLevel);
    }
}
