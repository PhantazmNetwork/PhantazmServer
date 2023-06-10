package org.phantazm.zombies.map.shop.display;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

@Model("zombies.map.shop.display.flag_conditional")
@Cache(false)
public class FlagConditionalDisplay implements ShopDisplay {
    private final Data data;
    private final ShopDisplay success;
    private final ShopDisplay failure;
    private final Flaggable flags;

    private ShopDisplay current;
    private Shop shop;
    private int ticks;

    @FactoryMethod
    public FlagConditionalDisplay(@NotNull Data data, @NotNull @Child("success") ShopDisplay success,
            @NotNull @Child("failure") ShopDisplay failure, @NotNull Flaggable flags) {
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

        current.initialize(shop);
    }

    @Override
    public void destroy(@NotNull Shop shop) {
        if (current == null) {
            return;
        }

        current.destroy(shop);
    }

    @Override
    public void update(@NotNull Shop shop, @NotNull PlayerInteraction interaction, boolean interacted) {
        if (current == null) {
            return;
        }

        current.update(shop, interaction, interacted);
    }

    @Override
    public void tick(long time) {
        if (shop == null) {
            return;
        }

        if (ticks++ % 20 == 0) {
            ShopDisplay oldCurrent = current;

            ShopDisplay newCurrent;
            if (flags.hasFlag(data.flag)) {
                newCurrent = success;
            }
            else {
                newCurrent = failure;
            }

            if (newCurrent != oldCurrent) {
                if (oldCurrent != null) {
                    oldCurrent.destroy(shop);
                }

                newCurrent.initialize(shop);
                current = newCurrent;
            }
        }

        if (current != null) {
            current.tick(time);
        }
    }

    @DataObject
    public record Data(@NotNull Key flag,
                       @NotNull @ChildPath("success") String success,
                       @NotNull @ChildPath("failure") String failure) {
    }
}
