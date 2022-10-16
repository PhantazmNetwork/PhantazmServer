package com.github.phantazmnetwork.zombies.game.map.shop.display;

import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.display.conditional")
@Cache(false)
public class ConditionalDisplay implements ShopDisplay {
    private final List<ShopDisplay> successDisplays;
    private final List<ShopDisplay> failureDisplays;

    private List<ShopDisplay> activeDisplays;

    @FactoryMethod
    public ConditionalDisplay(@NotNull Data data,
            @NotNull @DataName("success_displays") List<ShopDisplay> successDisplays,
            @NotNull @DataName("failure_displays") List<ShopDisplay> failureDisplays) {
        this.successDisplays = Objects.requireNonNull(successDisplays, "successDisplays");
        this.failureDisplays = Objects.requireNonNull(failureDisplays, "failureDisplays");
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        if (activeDisplays != null) {
            for (ShopDisplay display : activeDisplays) {
                display.initialize(shop);
            }
        }
    }

    @Override
    public void destroy(@NotNull Shop shop) {
        if (activeDisplays != null) {
            for (ShopDisplay display : activeDisplays) {
                display.destroy(shop);
            }

            activeDisplays = null;
        }
    }

    @Override
    public void update(@NotNull Shop shop, @NotNull PlayerInteraction interaction, boolean interacted) {
        List<ShopDisplay> newDisplays = interacted ? successDisplays : failureDisplays;
        if (newDisplays == activeDisplays) {
            for (ShopDisplay display : newDisplays) {
                display.update(shop, interaction, interacted);
            }

            return;
        }

        for (ShopDisplay display : activeDisplays) {
            display.destroy(shop);
        }

        for (ShopDisplay display : newDisplays) {
            display.initialize(shop);
        }

        activeDisplays = newDisplays;
    }

    @Override
    public void tick(long time) {
        if (activeDisplays != null) {
            for (ShopDisplay shopDisplay : activeDisplays) {
                shopDisplay.tick(time);
            }
        }
    }

    @DataObject
    public record Data(@NotNull @DataPath("success_displays") List<String> successDisplays,
                       @NotNull @DataPath("failure_displays") List<String> failureDisplays) {
    }
}
