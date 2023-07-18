package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.InteractorGroupHandler;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.List;
import java.util.Random;

@Model("zombies.map.shop.interactor.selection_group")
@Cache(false)
public class SelectionGroupInteractor implements ShopInteractor {
    private final Data data;
    private final List<ShopInteractor> inactiveInteractors;
    private final List<ShopInteractor> activeInteractors;

    private final InteractorGroupHandler handler;
    private final Random random;

    private boolean active;
    private Shop shop;

    @FactoryMethod
    public SelectionGroupInteractor(@NotNull Data data,
            @NotNull @Child("active_interactors") List<ShopInteractor> activeInteractors,
            @NotNull @Child("inactive_interactors") List<ShopInteractor> inactiveInteractors,
            @NotNull InteractorGroupHandler handler, @NotNull Random random) {
        this.data = data;
        this.activeInteractors = activeInteractors;
        this.inactiveInteractors = inactiveInteractors;
        this.handler = handler;
        this.random = random;

        handler.subscribe(data.group, this);
    }

    @Override
    public boolean handleInteraction(@NotNull PlayerInteraction interaction) {
        if (!active) {
            ShopInteractor.handle(inactiveInteractors, interaction);
            return false;
        }

        return ShopInteractor.handle(activeInteractors, interaction);
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        this.shop = shop;

        ShopInteractor.initialize(activeInteractors, shop);
        ShopInteractor.initialize(inactiveInteractors, shop);

        if (handler.isChosen(data.group)) {
            return;
        }

        List<SelectionGroupInteractor> interactors = handler.interactors(data.group);
        handler.markChosen(data.group);

        if (interactors.isEmpty()) {
            return;
        }

        interactors.get(random.nextInt(interactors.size())).setActive(true);
    }

    @Override
    public void tick(long time) {
        ShopInteractor.tick(activeInteractors, time);
        ShopInteractor.tick(inactiveInteractors, time);
    }

    public Shop shop() {
        return shop;
    }

    public void setActive(boolean active) {
        boolean currentlyActive = this.active;
        if (currentlyActive == active) {
            return;
        }

        if (active) {
            shop.flags().setFlag(data.group);
        }
        else {
            shop.flags().clearFlag(data.group);
        }

        this.active = active;
    }

    public boolean isActive() {
        return this.active;
    }

    @DataObject
    public record Data(@NotNull Key group,
                       @NotNull @ChildPath("active_interactors") List<String> activeInteractors,
                       @NotNull @ChildPath("inactive_interactors") List<String> inactiveInteractors) {
    }
}
