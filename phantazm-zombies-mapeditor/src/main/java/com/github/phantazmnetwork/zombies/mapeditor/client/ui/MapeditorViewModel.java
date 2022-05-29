package com.github.phantazmnetwork.zombies.mapeditor.client.ui;

import com.github.phantazmnetwork.commons.databind.Property;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;

public record MapeditorViewModel(@NotNull Property<Boolean> enabled,
                                 @NotNull Property<Vec3i> firstSelected,
                                 @NotNull Property<Vec3i> secondSelected,
                                 @NotNull Property<String> currentMapName) {

}
