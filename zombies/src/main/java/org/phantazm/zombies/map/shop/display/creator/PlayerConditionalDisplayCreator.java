package org.phantazm.zombies.map.shop.display.creator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.display.ShopDisplay;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Model("zombies.map.shop.display.player.conditional")
@Cache(false)
public class PlayerConditionalDisplayCreator implements PlayerDisplayCreator {
    private final Predicate<? super ZombiesPlayer> predicate;
    private final List<PlayerDisplayCreator> success;
    private final List<PlayerDisplayCreator> failure;

    @FactoryMethod
    public PlayerConditionalDisplayCreator(@NotNull @Child("predicate") Predicate<? super ZombiesPlayer> predicate,
        @NotNull @Child("success") List<PlayerDisplayCreator> success,
        @NotNull @Child("failure") List<PlayerDisplayCreator> failure) {
        this.predicate = predicate;
        this.success = success;
        this.failure = failure;
    }

    @Override
    public @NotNull ShopDisplay forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        List<ShopDisplay> success = new ArrayList<>(this.success.size());
        List<ShopDisplay> failure = new ArrayList<>(this.failure.size());

        for (PlayerDisplayCreator creator : this.success) {
            success.add(creator.forPlayer(zombiesPlayer));
        }

        for (PlayerDisplayCreator creator : this.failure) {
            failure.add(creator.forPlayer(zombiesPlayer));
        }

        return new Display(predicate, success, failure, zombiesPlayer);
    }

    private static class Display implements ShopDisplay {
        private final Predicate<? super ZombiesPlayer> predicate;
        private final List<ShopDisplay> success;
        private final List<ShopDisplay> failure;
        private final ZombiesPlayer zombiesPlayer;

        private Shop shop;
        private List<ShopDisplay> current;
        private int ticks;

        private Display(Predicate<? super ZombiesPlayer> predicate, List<ShopDisplay> success,
            List<ShopDisplay> failure, ZombiesPlayer zombiesPlayer) {
            this.predicate = predicate;
            this.success = success;
            this.failure = failure;
            this.zombiesPlayer = zombiesPlayer;
        }

        @Override
        public void initialize(@NotNull Shop shop) {
            this.shop = shop;
            this.current = initialize(shop, predicate.test(zombiesPlayer) ? this.success : this.failure);
        }

        private List<ShopDisplay> initialize(Shop shop, List<ShopDisplay> shopDisplays) {
            for (ShopDisplay shopDisplay : shopDisplays) {
                shopDisplay.initialize(shop);
            }

            return shopDisplays;
        }

        private void destroy(Shop shop, List<ShopDisplay> displays) {
            for (ShopDisplay display : displays) {
                display.destroy(shop);
            }
        }

        @Override
        public void destroy(@NotNull Shop shop) {
            List<ShopDisplay> current = this.current;
            if (current != null) {
                for (ShopDisplay shopDisplay : current) {
                    shopDisplay.destroy(shop);
                }

                this.current = null;
                this.shop = null;
            }
        }

        @Override
        public void update(@NotNull Shop shop, @NotNull PlayerInteraction interaction, boolean interacted) {
            List<ShopDisplay> current = this.current;
            if (current != null) {
                for (ShopDisplay display : current) {
                    display.update(shop, interaction, interacted);
                }
            }
        }

        @Override
        public void tick(long time) {
            List<ShopDisplay> current = this.current;
            Shop shop = this.shop;
            if (current == null || shop == null) {
                return;
            }

            if (ticks++ % 10 == 0) {
                boolean currentSuccess = current == this.success;
                boolean newSuccess = predicate.test(zombiesPlayer);
                if (currentSuccess == newSuccess) {
                    return;
                }

                destroy(shop, current);
                this.current = initialize(shop, newSuccess ? this.success : this.failure);
            }
        }
    }
}
