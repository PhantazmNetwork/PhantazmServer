package org.phantazm.zombies.map.shop.display;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.List;

@Model("zombies.map.shop.display.flag_conditional")
@Cache(false)
public class FlagConditionalDisplay implements ShopDisplay {
    private final Data data;
    private final List<ShopDisplay> success;
    private final List<ShopDisplay> failure;
    private final Flaggable flags;

    private List<ShopDisplay> current;
    private Shop shop;
    private int ticks;

    @FactoryMethod
    public FlagConditionalDisplay(@NotNull Data data, @NotNull @Child("success") List<ShopDisplay> success,
            @NotNull @Child("failure") List<ShopDisplay> failure, @NotNull Flaggable flags) {
        this.data = data;
        this.success = success;
        this.failure = failure;
        this.flags = flags;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        this.shop = shop;
        if (flags.hasFlag(data.flag)) {
            current = success;
        }
        else {
            current = failure;
        }

        for (ShopDisplay display : current) {
            display.initialize(shop);
        }
    }

    @Override
    public void destroy(@NotNull Shop shop) {
        if (current == null) {
            return;
        }

        for (ShopDisplay display : current) {
            display.destroy(shop);
        }
    }

    @Override
    public void update(@NotNull Shop shop, @NotNull PlayerInteraction interaction, boolean interacted) {
        if (current == null) {
            return;
        }

        for (ShopDisplay display : current) {
            display.update(shop, interaction, interacted);
        }
    }

    @Override
    public void tick(long time) {
        if (shop == null) {
            return;
        }

        if (ticks++ % 20 == 0) {
            List<ShopDisplay> oldCurrent = current;

            List<ShopDisplay> newCurrent;
            if (flags.hasFlag(data.flag)) {
                newCurrent = success;
            }
            else {
                newCurrent = failure;
            }

            if (newCurrent != oldCurrent) {
                if (oldCurrent != null) {
                    for (ShopDisplay display : oldCurrent) {
                        display.destroy(shop);
                    }
                }

                for (ShopDisplay display : newCurrent) {
                    display.initialize(shop);
                }
                current = newCurrent;
            }
        }

        if (current != null) {
            for (ShopDisplay display : current) {
                display.tick(time);
            }
        }
    }

    @DataObject
    public record Data(@NotNull Key flag,
                       @NotNull @ChildPath("success") List<String> success,
                       @NotNull @ChildPath("failure") List<String> failure) {
    }
}
