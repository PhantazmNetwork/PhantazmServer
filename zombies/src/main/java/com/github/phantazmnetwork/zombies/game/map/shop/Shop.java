package com.github.phantazmnetwork.zombies.game.map.shop;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.game.map.PositionalMapObject;
import com.github.phantazmnetwork.zombies.map.ShopInfo;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Shop extends PositionalMapObject<ShopInfo> implements Tickable {
    private final ShopInfo info;
    private final ShopHandler handler;

    public Shop(@NotNull ShopInfo info, @NotNull Vec3I origin, @NotNull Instance instance,
                @NotNull ShopHandler handler) {
        super(info, origin, instance);
        this.info = Objects.requireNonNull(info, "info");
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    public void initialize() {
        handler.initialize();
    }

    public void doInteract(@NotNull PlayerInteraction interaction) {
        handler.handleInteraction(interaction);
    }

    @Override
    public void tick(long time) {
        handler.tick(time);
    }
}
