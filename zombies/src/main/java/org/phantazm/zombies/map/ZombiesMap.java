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

public class ZombiesMap implements Tickable {
    private final MapObjects mapObjects;
    private final SongPlayer songPlayer;
    private final PowerupHandler powerupHandler;
    private final RoundHandler roundHandler;
    private final ShopHandler shopHandler;
    private final WindowHandler windowHandler;
    private final DoorHandler doorHandler;

    public ZombiesMap(@NotNull MapObjects mapObjects, @NotNull SongPlayer songPlayer,
        @NotNull PowerupHandler powerupHandler, @NotNull RoundHandler roundHandler,
        @NotNull ShopHandler shopHandler, @NotNull WindowHandler windowHandler, @NotNull DoorHandler doorHandler) {
        this.mapObjects = Objects.requireNonNull(mapObjects);
        this.songPlayer = Objects.requireNonNull(songPlayer);
        this.powerupHandler = Objects.requireNonNull(powerupHandler);
        this.roundHandler = Objects.requireNonNull(roundHandler);
        this.shopHandler = Objects.requireNonNull(shopHandler);
        this.windowHandler = Objects.requireNonNull(windowHandler);
        this.doorHandler = Objects.requireNonNull(doorHandler);
    }

    public @NotNull MapObjects mapObjects() {
        return mapObjects;
    }

    public @NotNull SongPlayer songPlayer() {
        return songPlayer;
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

    public @NotNull DoorHandler doorHandler() {
        return doorHandler;
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
