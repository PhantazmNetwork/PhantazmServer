package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.map.handler.RoundHandler;
import com.github.phantazmnetwork.zombies.map.handler.WindowHandler;
import com.github.phantazmnetwork.zombies.map.objects.MapObjects;
import com.github.phantazmnetwork.zombies.map.handler.ShopHandler;
import com.github.phantazmnetwork.zombies.powerup.PowerupHandler;
import org.jetbrains.annotations.NotNull;

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
