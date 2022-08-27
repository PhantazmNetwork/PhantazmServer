package com.github.phantazmnetwork.zombies.game.map.objects;

import com.github.phantazmnetwork.zombies.map.MapInfo;
import org.jetbrains.annotations.NotNull;

public interface MapObjectBuilder {
    @NotNull MapObjects build(@NotNull MapInfo mapInfo);
}
