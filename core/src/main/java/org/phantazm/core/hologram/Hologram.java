package org.phantazm.core.hologram;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents some floating text that renders in an {@link Instance}. The hologram's lines change in accordance to the
 * {@link Component} list this object represents.
 */
public interface Hologram extends List<Component> {
    /**
     * Updates the alignment of the hologram.
     *
     * @param alignment the new alignment
     */
    void setAlignment(@NotNull Alignment alignment);

    /**
     * Gets the current location of the hologram. Depending on the alignment used, this is either the center, bottom, or
     * top of the hologram lines.
     *
     * @return the center location of the hologram
     */
    @NotNull Point getLocation();

    /**
     * Sets the current location of the hologram.
     *
     * @param location the current location of the hologram
     */
    void setLocation(@NotNull Point location);

    /**
     * Sets the current instance of this Hologram.
     *
     * @param instance the new instance
     */
    void setInstance(@NotNull Instance instance);

    void setInstance(@NotNull Instance instance, @NotNull Point location);

    /**
     * Trims internal lists to size.
     */
    void trimToSize();

    enum Alignment {
        UPPER,
        CENTERED,
        LOWER
    }
}
