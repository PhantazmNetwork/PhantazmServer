package com.github.phantazmnetwork.zombies.map.shop.display;

import com.github.phantazmnetwork.core.hologram.Hologram;
import com.github.phantazmnetwork.core.hologram.InstanceHologram;
import com.github.steanky.vector.Vec3D;
import net.minestom.server.coordinate.Vec;

public abstract class HologramDisplayBase implements ShopDisplay {
    protected final Hologram hologram;

    public HologramDisplayBase() {
        //initial origin does not matter, the location will be set in initialize
        this.hologram = new InstanceHologram(Vec.ZERO, 0, Hologram.Alignment.LOWER);
    }
}
