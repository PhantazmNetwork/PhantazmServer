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
            for (ShopInteractor inactiveInteractor : inactiveInteractors) {
                inactiveInteractor.handleInteraction(interaction);
            }

            return false;
        }

        boolean res = true;
        for (ShopInteractor activeInteractor : activeInteractors) {
            res &= activeInteractor.handleInteraction(interaction);
        }

        return res;
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        this.shop = shop;

        for (ShopInteractor activeInteractor : activeInteractors) {
            activeInteractor.initialize(shop);
        }

        for (ShopInteractor inactiveInteractor : inactiveInteractors) {
            inactiveInteractor.initialize(shop);
        }

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
        for (ShopInteractor interactor : activeInteractors) {
            interactor.tick(time);
        }

        for (ShopInteractor interactor : inactiveInteractors) {
            interactor.tick(time);
        }
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
