package com.github.phantazmnetwork.api.hologram;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Hologram extends List<Component> {
    @NotNull Vec3D getLocation();
}
