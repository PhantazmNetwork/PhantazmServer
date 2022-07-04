package com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ShotEndpointSelector {

    @NotNull Optional<Point> getEnd(@NotNull Pos start);

    @NotNull VariantSerializable getData();

}
