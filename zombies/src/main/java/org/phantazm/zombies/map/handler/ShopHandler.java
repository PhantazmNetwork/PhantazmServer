package org.phantazm.zombies.map.handler;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.commons.Tickable;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.List;

public interface ShopHandler extends Tickable {
    void handleInteraction(@NotNull ZombiesPlayer player, @NotNull Point clicked, @NotNull Key interactionType);

    @NotNull @UnmodifiableView List<Shop> shops();

    interface Source {
        @NotNull ShopHandler make(@NotNull List<Shop> shops);
    }
}
