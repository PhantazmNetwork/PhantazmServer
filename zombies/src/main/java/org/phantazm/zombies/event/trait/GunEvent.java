package org.phantazm.zombies.event.trait;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.zombies.equipment.gun.Gun;

public interface GunEvent extends EquipmentEvent {
    @Override
    default @NotNull Equipment equipment() {
        return gun();
    }

    @NotNull Gun gun();
}
