package org.phantazm.zombies.map;

import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.sound.SongPlayer;
import org.phantazm.zombies.map.handler.DoorHandler;
import org.phantazm.zombies.map.handler.RoundHandler;
import org.phantazm.zombies.map.handler.ShopHandler;
import org.phantazm.zombies.map.handler.WindowHandler;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.powerup.PowerupHandler;

import java.util.Objects;

public record ZombiesMap(@NotNull MapObjects mapObjects,
    @NotNull SongPlayer songPlayer,
    @NotNull PowerupHandler powerupHandler,
    @NotNull RoundHandler roundHandler,
    @NotNull ShopHandler shopHandler,
    @NotNull WindowHandler windowHandler,
    @NotNull DoorHandler doorHandler) implements Tickable {
    public ZombiesMap(@NotNull MapObjects mapObjects, @NotNull SongPlayer songPlayer,
        @NotNull PowerupHandler powerupHandler, @NotNull RoundHandler roundHandler, @NotNull ShopHandler shopHandler,
        @NotNull WindowHandler windowHandler, @NotNull DoorHandler doorHandler) {
        this.mapObjects = Objects.requireNonNull(mapObjects);
        this.songPlayer = Objects.requireNonNull(songPlayer);
        this.powerupHandler = Objects.requireNonNull(powerupHandler);
        this.roundHandler = Objects.requireNonNull(roundHandler);
        this.shopHandler = Objects.requireNonNull(shopHandler);
        this.windowHandler = Objects.requireNonNull(windowHandler);
        this.doorHandler = Objects.requireNonNull(doorHandler);
    }

    @Override
    public void tick(long time) {
        songPlayer.tick(time);
        powerupHandler.tick(time);
        roundHandler.tick(time);
        shopHandler.tick(time);
        windowHandler.tick(time);
    }
}
