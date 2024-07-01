package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.InteractorGroupHandler;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

@Model("zombies.map.shop.interactor.selection_group")
@Cache(false)
public class SelectionGroupInteractor implements ShopInteractor {
    private final Data data;
    private final List<ShopInteractor> inactiveInteractors;
    private final List<ShopInteractor> activeInteractors;

    private final InteractorGroupHandler handler;
    private final Random random;
    private final List<Consumer<? super SelectionGroupInteractor>> initializationCallbacks;

    private boolean active;
    private Shop shop;

    @FactoryMethod
    public SelectionGroupInteractor(@NotNull Data data,
        @NotNull @Child("activeInteractors") List<ShopInteractor> activeInteractors,
        @NotNull @Child("inactiveInteractors") List<ShopInteractor> inactiveInteractors,
        @NotNull InteractorGroupHandler handler, @NotNull Random random) {
        this.data = data;
        this.activeInteractors = activeInteractors;
        this.inactiveInteractors = inactiveInteractors;
        this.handler = handler;
        this.random = random;
        this.initializationCallbacks = new ArrayList<>();

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

        for (Consumer<? super SelectionGroupInteractor> consumer : initializationCallbacks) {
            consumer.accept(this);
        }

        initializationCallbacks.clear();

        if (handler.isChosen(data.group)) {
            return;
        }

        List<SelectionGroupInteractor> interactors = handler.interactors(data.group);
        handler.markChosen(data.group);

        if (interactors.isEmpty()) {
            return;
        }

        interactors.get(random.nextInt(interactors.size()))
            .addInitializationCallback(interactor -> interactor.setActive(true));
    }

    @Override
    public void tick(long time) {
        ShopInteractor.tick(activeInteractors, time);
        ShopInteractor.tick(inactiveInteractors, time);
    }

    public Shop shop() {
        return shop;
    }

    private void addInitializationCallback(@NotNull Consumer<? super SelectionGroupInteractor> consumer) {
        if (this.shop == null) {
            initializationCallbacks.add(consumer);
        } else {
            consumer.accept(this);
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        boolean currentlyActive = this.active;
        if (currentlyActive == active) {
            return;
        }

        if (active) {
            shop.flags().setFlag(data.group);
        } else {
            shop.flags().clearFlag(data.group);
        }

        this.active = active;
    }

    @DataObject
    public record Data(@NotNull Key group) {
    }
}
