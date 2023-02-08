package org.phantazm.zombies.map.handler;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.commons.Tickable;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface ShopHandler extends Tickable {
    void handleInteraction(@NotNull ZombiesPlayer player, @NotNull Point clicked, @NotNull Key interactionType);

    @NotNull @UnmodifiableView BoundedTracker<Shop> tracker();

    void initialize();

    interface Source {
        @NotNull ShopHandler make(@NotNull BoundedTracker<Shop> shops);
    }
}
