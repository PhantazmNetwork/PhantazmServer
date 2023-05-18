package org.phantazm.zombies.map.shop.display;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.zombies.map.shop.Shop;

import java.util.Objects;

public abstract class HologramDisplayBase implements ShopDisplay {
    protected final Hologram hologram;

    public HologramDisplayBase(@NotNull Hologram hologram) {
        //initial origin does not matter, the location will be set in initialize
        this.hologram = Objects.requireNonNull(hologram, "hologram");
    }

    @Override
    public void destroy(@NotNull Shop shop) {
        hologram.clear();
    }
}
