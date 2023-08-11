package org.phantazm.zombies.map.shop.display;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.ShopFlagSource;

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

        List<ShopDisplay> current = switch (data.source) {
            case MAP -> flags.hasFlag(data.flag) ? success : failure;
            case SHOP -> shop.flags().hasFlag(data.flag) ? success : failure;
        };

        for (ShopDisplay display : current) {
            display.initialize(shop);
        }

        this.current = current;
    }

    @Override
    public void destroy(@NotNull Shop shop) {
        List<ShopDisplay> current = this.current;
        if (current == null) {
            return;
        }

        for (ShopDisplay display : current) {
            display.destroy(shop);
        }
    }

    @Override
    public void update(@NotNull Shop shop, @NotNull PlayerInteraction interaction, boolean interacted) {
        List<ShopDisplay> current = this.current;
        if (current == null) {
            return;
        }

        for (ShopDisplay display : current) {
            display.update(shop, interaction, interacted);
        }
    }

    @Override
    public void tick(long time) {
        Shop shop = this.shop;
        if (shop == null) {
            return;
        }

        if (ticks++ % 4 == 0) {
            List<ShopDisplay> oldCurrent = current;
            List<ShopDisplay> newCurrent = hasFlag() ? success : failure;

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

        List<ShopDisplay> current = this.current;
        if (current != null) {
            for (ShopDisplay display : current) {
                display.tick(time);
            }
        }
    }

    private boolean hasFlag() {
        Shop shop = this.shop;
        return switch (data.source) {
            case MAP -> flags.hasFlag(data.flag);
            case SHOP -> shop != null && shop.flags().hasFlag(data.flag);
        };
    }

    @DataObject
    public record Data(@NotNull Key flag,
                       @NotNull ShopFlagSource source,
                       @NotNull @ChildPath("success") List<String> success,
                       @NotNull @ChildPath("failure") List<String> failure) {
        @Default("source")
        public static @NotNull ConfigElement defaultShopFlagSource() {
            return ConfigPrimitive.of("MAP");
        }
    }
}
