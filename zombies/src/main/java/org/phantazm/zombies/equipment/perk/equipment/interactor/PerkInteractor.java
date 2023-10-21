package org.phantazm.zombies.equipment.perk.equipment.interactor;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface PerkInteractor {
    boolean setSelected(boolean selected);

    boolean leftClick();

    boolean rightClick();

    boolean attack(@NotNull Entity target);
}
