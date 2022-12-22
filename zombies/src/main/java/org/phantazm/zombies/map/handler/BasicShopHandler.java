package org.phantazm.zombies.map.handler;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.VecUtils;
import org.phantazm.zombies.map.BasicPlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BasicShopHandler implements ShopHandler {
    private final List<Shop> shops;
    private final List<Shop> shopView;

    public BasicShopHandler(@NotNull List<Shop> shops) {
        this.shops = Objects.requireNonNull(shops, "shops");
        this.shopView = Collections.unmodifiableList(shops);
    }

    @Override
    public void tick(long time) {
        for (Shop shop : shops) {
            shop.tick(time);
        }
    }

    public void handleInteraction(@NotNull ZombiesPlayer player, @NotNull Point clicked, @NotNull Key interactionType) {
        for (Shop shop : shops) {
            if (shop.getShopInfo().triggerLocation().equals(VecUtils.toBlockInt(clicked))) {
                shop.handleInteraction(new BasicPlayerInteraction(player, interactionType));
            }
        }
    }

    @Override
    public @NotNull @UnmodifiableView List<Shop> shops() {
        return shopView;
    }
}
