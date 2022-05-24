package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record MapInfo(@NotNull String name, @NotNull String displayName, @NotNull ItemStack displayItem,
                      @NotNull Vec3I origin, @NotNull String roomsPath, @NotNull String windowsPath,
                      @NotNull String roundsPath, int version) { }
