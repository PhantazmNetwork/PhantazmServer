package com.github.phantazmnetwork.zombies.equipment.gun.target.tester;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

@FunctionalInterface
public interface TargetTester {

    boolean useTarget(@NotNull Entity target, @NotNull Collection<UUID> previousHits);

}
