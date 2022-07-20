package com.github.phantazmnetwork.zombies.game.map.shop;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.game.map.PositionalMapObject;
import com.github.phantazmnetwork.zombies.game.map.shop.display.ShopDisplay;
import com.github.phantazmnetwork.zombies.game.map.shop.interactor.ShopInteractor;
import com.github.phantazmnetwork.zombies.map.ShopInfo;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Shop extends PositionalMapObject<ShopInfo> implements Tickable {
    private final ShopInteractor interactor;
    private final ShopDisplay display;

    public Shop(@NotNull ShopInfo info, @NotNull Vec3I origin, @NotNull Instance instance,
                @NotNull ShopInteractor interactor, @NotNull ShopDisplay display) {
        super(info, origin, instance);
        this.interactor = Objects.requireNonNull(interactor, "interactionHandler");
        this.display = Objects.requireNonNull(display, "display");
    }

    public @NotNull ShopInteractor getInteractor() {
        return interactor;
    }

    public @NotNull ShopDisplay getDisplay() {
        return display;
    }

    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        if (interactor.handleInteraction(this, interaction)) {
            display.update(this, interaction);
        }
    }

    @Override
    public void tick(long time) {
        interactor.tick(time);
        display.tick(time);
    }
}
