package com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface DirectionalEntityFinder {

    @NotNull Collection<Entity> findEntities(@NotNull Instance instance, @NotNull Pos start, @NotNull Point end);

    @NotNull VariantSerializable getData();

}
