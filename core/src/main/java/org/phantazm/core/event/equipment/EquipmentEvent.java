package org.phantazm.core.event.equipment;

import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;

public interface EquipmentEvent extends PlayerEvent {
    @NotNull Equipment equipment();
}
