package com.github.phantazmnetwork.zombies.map.shop.display;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.core.hologram.Hologram;
import com.github.phantazmnetwork.core.hologram.InstanceHologram;

public abstract class HologramDisplayBase implements ShopDisplay {
    protected final Hologram hologram;

    public HologramDisplayBase() {
        //initial origin does not matter, the location will be set in initialize
        this.hologram = new InstanceHologram(Vec3D.ORIGIN, 0, Hologram.Alignment.LOWER);
    }
}
