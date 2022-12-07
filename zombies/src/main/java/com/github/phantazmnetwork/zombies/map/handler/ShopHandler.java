package com.github.phantazmnetwork.zombies.map.handler;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.map.shop.Shop;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

public interface ShopHandler extends Tickable {
    void handleInteraction(@NotNull ZombiesPlayer player, @NotNull Point clicked, @NotNull Key interactionType);

    @NotNull @UnmodifiableView List<Shop> shops();

    interface Source {
        @NotNull ShopHandler make(@NotNull List<Shop> shops);
    }
}
