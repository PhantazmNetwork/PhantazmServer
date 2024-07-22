package org.phantazm.zombies.event.trait;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;

public interface EquipmentEvent extends Event {
    @NotNull Equipment equipment();
}
