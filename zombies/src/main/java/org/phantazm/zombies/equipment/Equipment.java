package org.phantazm.zombies.equipment;

import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.Player;
import org.phantazm.core.inventory.InventoryObject;

/**
 * Represents a piece of equipment that {@link Player}s may use.
 */
public interface Equipment extends InventoryObject, Keyed {

    /**
     * Invoked when a {@link Player} changes their slot to or from the one containing this {@link Equipment}.
     *
     * @param selected Whether the {@link Player} is now holding this {@link Equipment}
     */
    void setSelected(boolean selected);

    /**
     * Invoked when a {@link Player} right clicks with this {@link Equipment}.
     */
    void rightClick();

    /**
     * Invoked when a {@link Player} left clicks with this {@link Equipment}.
     */
    void leftClick();

}
