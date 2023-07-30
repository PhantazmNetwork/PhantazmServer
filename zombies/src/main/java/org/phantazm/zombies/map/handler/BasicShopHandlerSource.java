package org.phantazm.zombies.map.handler;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.map.shop.Shop;

public class BasicShopHandlerSource implements ShopHandler.Source {
    @Override
    public @NotNull ShopHandler make(@NotNull BoundedTracker<Shop> shops, @NotNull BoundedTracker<Room> rooms) {
        return new BasicShopHandler(shops, rooms);
    }
}
