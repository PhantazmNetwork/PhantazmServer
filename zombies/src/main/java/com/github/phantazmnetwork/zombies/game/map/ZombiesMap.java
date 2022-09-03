package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.game.coin.ModifierSource;
import com.github.phantazmnetwork.zombies.game.map.objects.MapObjects;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import com.github.phantazmnetwork.zombies.map.*;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ZombiesMap extends PositionalMapObject<MapInfo> implements Tickable, Flaggable.Source {
    private final ModifierSource modifierSource;
    private final Flaggable flags;

    private final MapObjects mapObjects;
    private final RoundHandler roundHandler;

    public ZombiesMap(@NotNull MapInfo info, @NotNull Instance instance, @NotNull ModifierSource modifierSource,
            @NotNull Flaggable flags, @NotNull MapObjects mapObjects, @NotNull RoundHandler roundHandler) {
        super(info, info.settings().origin(), instance);
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
