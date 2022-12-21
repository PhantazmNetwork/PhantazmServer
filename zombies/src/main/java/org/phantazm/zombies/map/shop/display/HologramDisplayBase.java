package org.phantazm.zombies.map.shop.display;

import net.minestom.server.coordinate.Vec;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.hologram.InstanceHologram;

public abstract class HologramDisplayBase implements ShopDisplay {
    protected final Hologram hologram;

    public HologramDisplayBase() {
        //initial origin does not matter, the location will be set in initialize
        this.hologram = new InstanceHologram(Vec.ZERO, 0, Hologram.Alignment.LOWER);
    }
}
