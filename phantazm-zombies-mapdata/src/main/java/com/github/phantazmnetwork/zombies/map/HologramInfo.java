package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record HologramInfo(@NotNull Component text,
                           @NotNull Vec3D position) { }
