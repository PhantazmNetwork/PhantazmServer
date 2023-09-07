package org.phantazm.zombies.equipment.gun2.event;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record GunEmptyEvent(@NotNull UUID gunUUID) implements Event {

}
