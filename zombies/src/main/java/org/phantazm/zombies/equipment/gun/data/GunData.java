package org.phantazm.zombies.equipment.gun.data;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * General data for a gun.
 *
 * @param name The unique {@link Key} name of the gun
 */
public record GunData(@NotNull Key name, @NotNull Key rootLevel) {

    /**
     * Creates a {@link GunData}.
     *
     * @param name The unique {@link Key} name of the gun
     */
    public GunData {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(rootLevel, "rootLevel");
    }

}
