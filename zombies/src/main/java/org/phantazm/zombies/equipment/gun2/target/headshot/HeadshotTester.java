package org.phantazm.zombies.equipment.gun2.target.headshot;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface HeadshotTester {

    boolean isHeadshot(@NotNull Entity shooter, @NotNull Entity entity, @NotNull Point intersection);

}
