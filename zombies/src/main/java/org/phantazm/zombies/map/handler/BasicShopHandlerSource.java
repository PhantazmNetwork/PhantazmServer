package org.phantazm.zombies.map.handler;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.Shop;

import java.util.List;

public class BasicShopHandlerSource implements ShopHandler.Source {
    @Override
    public @NotNull ShopHandler make(@NotNull List<Shop> shops) {
        return new BasicShopHandler(shops);
    }
}
