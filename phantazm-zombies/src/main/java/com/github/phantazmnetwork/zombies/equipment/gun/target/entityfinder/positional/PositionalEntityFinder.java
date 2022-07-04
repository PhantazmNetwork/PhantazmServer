package com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.positional;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface PositionalEntityFinder {

    @NotNull Collection<Entity> findEntities(@NotNull Instance instance, @NotNull Point start);

    @NotNull VariantSerializable getData();

}
