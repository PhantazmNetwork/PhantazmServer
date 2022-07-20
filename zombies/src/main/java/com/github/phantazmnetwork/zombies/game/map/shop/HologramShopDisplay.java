package com.github.phantazmnetwork.zombies.game.map.shop;

import com.github.phantazmnetwork.commons.component.annotation.ComponentData;
import com.github.phantazmnetwork.commons.component.annotation.ComponentFactory;
import com.github.phantazmnetwork.commons.component.annotation.ComponentModel;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@ComponentModel("phantazm:zombies.map.shop.display.hologram")
public class HologramShopDisplay implements ShopDisplay {
    private final Data data;

    @ComponentFactory
    public HologramShopDisplay(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public void tick(long time) {

    }

    @Override
    public void initialize(@NotNull Shop shop) {
        Vec3I location = shop.getData().triggerLocation();
        Vec3D center = Vec3D.of(location.getX() + 0.5, location.getY() + 0.5, location.getZ() + 0.5);
    }

    @Override
    public void update(@NotNull Shop shop, @NotNull PlayerInteraction interaction) {

    }

    @ComponentData
    public record Data(@NotNull List<Component> display, @NotNull Vec3D offset) {
    }
}
