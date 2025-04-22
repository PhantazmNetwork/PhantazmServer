package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.Cooldown;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.interactor.cooldown")
@Cache(false)
public class CooldownInteractor extends InteractorBase<CooldownInteractor.Data> {
    private final List<ShopInteractor> successInteractors;
    private final List<ShopInteractor> failureInteractors;
    private final Cooldown cooldown;

    @FactoryMethod
    public CooldownInteractor(@NotNull Data data,
        @Child("successInteractors") List<ShopInteractor> successInteractors,
        @Child("failureInteractors") List<ShopInteractor> failureInteractors) {
        super(data);
        this.successInteractors = Objects.requireNonNull(successInteractors);
        this.failureInteractors = Objects.requireNonNull(failureInteractors);
        this.cooldown = Cooldown.cooldown();
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        if (data.cooldown < 0 || cooldown.takeCooldown(data.cooldown))
            ShopInteractor.handle(successInteractors, interaction);
        else ShopInteractor.handle(failureInteractors, interaction);

        return true;
    }

    @Override
    public void tick(long time) {
        ShopInteractor.tick(successInteractors, time);
        ShopInteractor.tick(failureInteractors, time);

        cooldown.step();
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        ShopInteractor.initialize(successInteractors, shop);
        ShopInteractor.initialize(failureInteractors, shop);
    }

    @DataObject
    public record Data(int cooldown) {

    }
}
