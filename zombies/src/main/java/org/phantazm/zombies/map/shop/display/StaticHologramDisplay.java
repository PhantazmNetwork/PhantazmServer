package org.phantazm.zombies.map.shop.display;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.VecUtils;
import org.phantazm.zombies.map.HologramInfo;
import org.phantazm.zombies.map.shop.Shop;

import java.util.Objects;

@Model("zombies.map.shop.display.static_hologram")
public class StaticHologramDisplay extends HologramDisplayBase {
    private final Data data;

    @FactoryMethod
    public StaticHologramDisplay(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        hologram.setInstance(shop.getInstance(), shop.computeAbsolutePosition(VecUtils.toPoint(data.info.position())));
        hologram.clear();

        hologram.addAll(data.info.text());
    }

    @Override
    public void destroy(@NotNull Shop shop) {
        hologram.clear();
    }

    @DataObject
    public record Data(@NotNull HologramInfo info) {
    }
}