package org.phantazm.zombies.map;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.map.handler.ShopHandler;
import org.phantazm.zombies.map.handler.WindowHandler;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.powerup.PowerupHandler;

import java.util.Objects;

public class ZombiesMap implements Tickable {
    private final MapObjects mapObjects;
    private final PowerupHandler powerupHandler;
    private final RoundHandler roundHandler;
    private final ShopHandler shopHandler;
    private final WindowHandler windowHandler;

    public ZombiesMap(@NotNull MapObjects mapObjects, @NotNull PowerupHandler powerupHandler,
            @NotNull RoundHandler roundHandler, @NotNull ShopHandler shopHandler,
            @NotNull WindowHandler windowHandler) {
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
        this.powerupHandler = Objects.requireNonNull(powerupHandler, "powerupHandler");
        this.roundHandler = Objects.requireNonNull(roundHandler, "roundHandler");
        this.shopHandler = Objects.requireNonNull(shopHandler, "shopHandler");
        this.windowHandler = Objects.requireNonNull(windowHandler, "windowHandler");
    }

    public @NotNull MapObjects mapObjects() {
        return mapObjects;
    }

    public @NotNull PowerupHandler powerupHandler() {
        return powerupHandler;
    }

    public @NotNull RoundHandler roundHandler() {
        return roundHandler;
    }

    public @NotNull ShopHandler shopHandler() {
        return shopHandler;
    }

    public @NotNull WindowHandler windowHandler() {
        return windowHandler;
    }

    @Override
    public void tick(long time) {
        powerupHandler.tick(time);
        roundHandler.tick(time);
        shopHandler.tick(time);
        windowHandler.tick(time);
    }
}
