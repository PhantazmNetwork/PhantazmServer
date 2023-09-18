package org.phantazm.zombies.equipment.gun2.shoot.fire;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

@FunctionalInterface
public interface Firer {

    void fire(@NotNull Pos start, @NotNull Collection<UUID> previousHits);

}