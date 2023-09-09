package org.phantazm.zombies.equipment.gun2.event;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun2.shoot.GunShot;

public record GunHitShotEvent(@NotNull GunShot hit) implements Event {
}
