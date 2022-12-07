package com.github.phantazmnetwork.zombies.map.handler;

import com.github.phantazmnetwork.zombies.map.shop.Shop;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BasicShopHandlerSource implements ShopHandler.Source {
    @Override
    public @NotNull ShopHandler make(@NotNull List<Shop> shops) {
        return new BasicShopHandler(shops);
    }
}
