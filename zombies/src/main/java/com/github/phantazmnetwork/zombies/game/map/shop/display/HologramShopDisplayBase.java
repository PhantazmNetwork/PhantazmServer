package com.github.phantazmnetwork.zombies.game.map.shop.display;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.core.hologram.Hologram;
import com.github.phantazmnetwork.core.hologram.InstanceHologram;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import org.jetbrains.annotations.NotNull;

public abstract class HologramShopDisplayBase implements ShopDisplay {
    protected final Hologram hologram;

    public HologramShopDisplayBase() {
        //initial origin does not matter, the location will be set in initialize
        this.hologram = new InstanceHologram(Vec3D.ORIGIN, 0, Hologram.Alignment.LOWER);
    }

    @Override
    public void tick(long time) {
    }

    @Override
    public void update(@NotNull Shop shop, @NotNull PlayerInteraction interaction, boolean interactionResult) {
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        Vec3I location = shop.getData().triggerLocation();
        Vec3D center = Vec3D.of(location.getX() + 0.5, location.getY() + 0.5, location.getZ() + 0.5);

        hologram.setLocation(center);
        hologram.setInstance(shop.getInstance());
    }
}
