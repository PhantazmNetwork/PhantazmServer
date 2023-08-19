package org.phantazm.zombies.map.shop.display;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.display.conditional")
@Cache(false)
public class ConditionalDisplay implements ShopDisplay {
    private final List<ShopDisplay> successDisplays;
    private final List<ShopDisplay> failureDisplays;

    private List<ShopDisplay> activeDisplays;

    @FactoryMethod
    public ConditionalDisplay(@NotNull @Child("success_displays") List<ShopDisplay> successDisplays,
            @NotNull @Child("failure_displays") List<ShopDisplay> failureDisplays) {
        this.successDisplays = Objects.requireNonNull(successDisplays);
        this.failureDisplays = Objects.requireNonNull(failureDisplays);
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        List<ShopDisplay> activeDisplays = this.activeDisplays;
        if (activeDisplays != null) {
            for (ShopDisplay display : activeDisplays) {
                display.initialize(shop);
            }
        }
    }

    @Override
    public void destroy(@NotNull Shop shop) {
        List<ShopDisplay> activeDisplays = this.activeDisplays;
        if (activeDisplays != null) {
            for (ShopDisplay display : activeDisplays) {
                display.destroy(shop);
            }

            this.activeDisplays = null;
        }
    }

    @Override
    public void update(@NotNull Shop shop, @NotNull PlayerInteraction interaction, boolean interacted) {
        List<ShopDisplay> newDisplays = interacted ? successDisplays : failureDisplays;
        List<ShopDisplay> activeDisplays = this.activeDisplays;
        if (newDisplays == activeDisplays) {
            for (ShopDisplay display : newDisplays) {
                display.update(shop, interaction, interacted);
            }

            return;
        }

        if (activeDisplays != null) {
            for (ShopDisplay display : activeDisplays) {
                display.destroy(shop);
            }
        }

        for (ShopDisplay display : newDisplays) {
            display.initialize(shop);
        }

        this.activeDisplays = newDisplays;
    }

    @Override
    public void tick(long time) {
        List<ShopDisplay> activeDisplays = this.activeDisplays;
        if (activeDisplays != null) {
            for (ShopDisplay shopDisplay : activeDisplays) {
                shopDisplay.tick(time);
            }
        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("success_displays") List<String> successDisplays,
                       @NotNull @ChildPath("failure_displays") List<String> failureDisplays) {
    }
}
