package org.phantazm.zombies.map.handler;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.zombies.map.BasicPlayerInteraction;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.stage.StageKeys;

import java.util.Objects;
import java.util.Optional;

public class BasicShopHandler implements ShopHandler {
    private final BoundedTracker<Shop> shopTracker;
    private final BoundedTracker<Room> roomTracker;

    public BasicShopHandler(@NotNull BoundedTracker<Shop> shopTracker, @NotNull BoundedTracker<Room> roomTracker) {
        this.shopTracker = Objects.requireNonNull(shopTracker, "shopTracker");
        this.roomTracker = Objects.requireNonNull(roomTracker, "roomTracker");
    }

    @Override
    public void tick(long time) {
        for (Shop shop : shopTracker.items()) {
            shop.tick(time);
        }
    }

    public boolean handleInteraction(@NotNull ZombiesPlayer player, @NotNull Point clicked,
            @NotNull Key interactionType) {
        if (!player.inStage(StageKeys.IN_GAME)) {
            return false;
        }

        Wrapper<Boolean> result = Wrapper.of(false);
        shopTracker.atPoint(clicked).ifPresent(shop -> {
            Optional<Room> roomOptional = roomTracker.atPoint(shop.center());
            if (roomOptional.isPresent()) {
                if (!roomOptional.get().isOpen()) {
                    return;
                }
            }

            shop.handleInteraction(new BasicPlayerInteraction(player, interactionType));
            result.set(true);
        });

        return result.get();
    }

    @Override
    public @NotNull BoundedTracker<Shop> tracker() {
        return shopTracker;
    }

    @Override
    public void initialize() {
        for (Shop shop : shopTracker.items()) {
            shop.initialize();
        }
    }
}
