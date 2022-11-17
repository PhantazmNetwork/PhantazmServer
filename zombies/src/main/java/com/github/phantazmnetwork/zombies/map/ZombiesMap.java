package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.coin.TransactionModifierSource;
import com.github.phantazmnetwork.zombies.map.objects.MapObjects;
import com.github.phantazmnetwork.zombies.powerup.PowerupHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class ZombiesMap implements Tickable, Flaggable.Source {
    private final TransactionModifierSource transactionModifierSource;
    private final Flaggable flags;

    private final MapObjects mapObjects;
    private final PowerupHandler powerupHandler;
    private final RoundHandler roundHandler;

    public ZombiesMap(@NotNull TransactionModifierSource transactionModifierSource, @NotNull Flaggable flags,
            @NotNull MapObjects mapObjects, @NotNull PowerupHandler powerupHandler,
            @NotNull RoundHandler roundHandler) {
        this.transactionModifierSource = Objects.requireNonNull(transactionModifierSource, "modifierSource");
        this.flags = Objects.requireNonNull(flags, "flags");
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
        this.powerupHandler = Objects.requireNonNull(powerupHandler, "powerupHandler");
        this.roundHandler = Objects.requireNonNull(roundHandler, "roundHandler");
    }

    public @NotNull TransactionModifierSource modifierSource() {
        return transactionModifierSource;
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

    @Override
    public @NotNull Flaggable flags() {
        return flags;
    }
}
