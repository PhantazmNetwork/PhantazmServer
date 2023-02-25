package org.phantazm.zombies.equipment;

import net.kyori.adventure.key.Key;
import org.phantazm.commons.Namespaces;

public final class EquipmentTypes {
    private EquipmentTypes() {
    }

    public static final Key GUN = Key.key(Namespaces.PHANTAZM, "gun");
    public static final Key PERK = Key.key(Namespaces.PHANTAZM, "perk");
}
