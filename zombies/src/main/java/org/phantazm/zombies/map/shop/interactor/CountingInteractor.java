package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.List;

@Model("zombies.map.shop.interactor.counting")
@Cache(false)
public class CountingInteractor extends InteractorBase<CountingInteractor.Data> {
    private final List<ShopInteractor> successInteractors;
    private final List<ShopInteractor> failureInteractors;

    private int uses;

    @FactoryMethod
    public CountingInteractor(@NotNull Data data, @NotNull @Child("success") List<ShopInteractor> successInteractors,
        @NotNull @Child("failure") List<ShopInteractor> failureInteractors) {
        super(data);
        this.successInteractors = successInteractors;
        this.failureInteractors = failureInteractors;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        ShopInteractor.initialize(successInteractors, shop);
        ShopInteractor.initialize(failureInteractors, shop);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        int uses = this.uses;

        if (uses < data.maxUses) {
            ShopInteractor.handle(successInteractors, interaction);
        }

        boolean success = true;
        if (data.reset) {
            int nextUse = ++uses % data.maxUses;
            if (nextUse < uses) {
                success = false;
                ShopInteractor.handle(failureInteractors, interaction);
            }

            this.uses = nextUse;
            return success;
        }

        int nextUse = ++uses;
        if (nextUse >= data.maxUses) {
            success = false;
            ShopInteractor.handle(failureInteractors, interaction);
        } else {
            this.uses = nextUse;
        }

        return success;
    }

    @Override
    public void tick(long time) {
        ShopInteractor.tick(successInteractors, time);
        ShopInteractor.tick(failureInteractors, time);
    }

    @DataObject
    public record Data(
        int maxUses,
        boolean reset,
        @NotNull @ChildPath("success") List<String> successInteractors,
        @NotNull @ChildPath("failure") List<String> failureInteractors) {
        @Default("reset")
        public static @NotNull ConfigElement resetDefault() {
            return ConfigPrimitive.of(false);
        }
    }
}
