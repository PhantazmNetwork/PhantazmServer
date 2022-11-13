package com.github.phantazmnetwork.zombies.map.objects;

import com.github.phantazmnetwork.zombies.map.MapInfo;
import com.github.steanky.element.core.dependency.DependencyModule;
import org.jetbrains.annotations.NotNull;

public interface MapObjectBuilder {
    @NotNull MapObjects build(@NotNull MapInfo mapInfo);
}
