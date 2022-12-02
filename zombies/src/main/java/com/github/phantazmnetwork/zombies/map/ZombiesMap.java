package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.coin.TransactionModifierSource;
import com.github.phantazmnetwork.zombies.map.objects.MapObjects;
import com.github.phantazmnetwork.zombies.powerup.PowerupHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ZombiesMap implements Tickable {
    private final MapObjects mapObjects;
    private final PowerupHandler powerupHandler;
    private final RoundHandler roundHandler;

    public ZombiesMap(@NotNull MapObjects mapObjects, @NotNull PowerupHandler powerupHandler,
            @NotNull RoundHandler roundHandler) {
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
        this.powerupHandler = Objects.requireNonNull(powerupHandler, "powerupHandler");
        this.roundHandler = Objects.requireNonNull(roundHandler, "roundHandler");
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

    @Override
    public void tick(long time) {
        mapObjects.tick(time);
        powerupHandler.tick(time);
        roundHandler.tick(time);
    }
}
