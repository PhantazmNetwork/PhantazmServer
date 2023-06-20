package org.phantazm.zombies.equipment.gun.event;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

public record GunShootEvent(@NotNull Gun gun, @NotNull GunShot shot) implements Event {

}
