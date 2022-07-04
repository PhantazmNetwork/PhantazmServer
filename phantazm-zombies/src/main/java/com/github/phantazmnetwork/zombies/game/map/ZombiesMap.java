package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.map.MapInfo;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public class ZombiesMap extends MapObject<MapInfo> {
    /**
     * Constructs a new instance of this class.
     *
     * @param info     the backing data object
     * @param origin   the origin vector this object's coordinates are considered relative to
     * @param instance the instance which this MapObject is in
     */
    public ZombiesMap(@NotNull MapInfo info, @NotNull Vec3I origin, @NotNull Instance instance) {
        super(info, origin, instance);
    }
}
