package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.coin.ModifierSource;
import com.github.phantazmnetwork.zombies.map.objects.MapObjects;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class ZombiesMap implements Tickable, Flaggable.Source {
    private final ModifierSource modifierSource;
    private final Flaggable flags;

    private final MapObjects mapObjects;
    private final RoundHandler roundHandler;

    public ZombiesMap(@NotNull ModifierSource modifierSource, @NotNull Flaggable flags, @NotNull MapObjects mapObjects,
            @NotNull RoundHandler roundHandler) {
        this.modifierSource = Objects.requireNonNull(modifierSource, "modifierSource");
        this.flags = Objects.requireNonNull(flags, "flags");
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
        this.roundHandler = Objects.requireNonNull(roundHandler, "roundHandler");
    }

    public @NotNull ModifierSource modifierSource() {
        return modifierSource;
    }

    public @NotNull MapObjects mapObjects() {
        return mapObjects;
    }

    public @NotNull RoundHandler roundHandler() {
        return roundHandler;
    }

    @Override
    public void tick(long time) {
        roundHandler.tick(time);
        mapObjects.tick(time);
    }

    @Override
    public @NotNull Flaggable flags() {
        return flags;
    }
}
