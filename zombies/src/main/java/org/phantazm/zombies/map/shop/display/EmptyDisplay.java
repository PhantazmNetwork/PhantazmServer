package org.phantazm.zombies.map.shop.display;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.Shop;

@Model("zombies.map.shop.display.empty")
@Cache
public class EmptyDisplay implements ShopDisplay {
    @FactoryMethod
    public EmptyDisplay() {
    }

    @Override
    public void initialize(@NotNull Shop shop) {

    }

    @Override
    public void destroy(@NotNull Shop shop) {

    }
}
