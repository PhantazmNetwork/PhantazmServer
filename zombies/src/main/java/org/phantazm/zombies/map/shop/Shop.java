package org.phantazm.zombies.map.shop;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.Tickable;
import org.phantazm.core.tracker.BoundedBase;
import org.phantazm.zombies.map.ShopInfo;
import org.phantazm.zombies.map.shop.display.ShopDisplay;
import org.phantazm.zombies.map.shop.interactor.ShopInteractor;
import org.phantazm.zombies.map.shop.predicate.ShopPredicate;

import java.util.List;
import java.util.Objects;

public class Shop extends BoundedBase implements Tickable {
    private final Point mapOrigin;
    private final Instance instance;
    private final ShopInfo shopInfo;

    private final List<ShopPredicate> predicates;
    private final List<ShopInteractor> successInteractors;
    private final List<ShopInteractor> failureInteractors;
    private final List<ShopDisplay> displays;

    public Shop(@NotNull Point mapOrigin, @NotNull ShopInfo shopInfo, @NotNull Instance instance,
            @NotNull List<ShopPredicate> predicates, @NotNull List<ShopInteractor> successInteractors,
            @NotNull List<ShopInteractor> failureInteractors, @NotNull List<ShopDisplay> displays) {
        super(mapOrigin, shopInfo.trigger());

        this.mapOrigin = mapOrigin;
        this.instance = Objects.requireNonNull(instance, "instance");
        this.shopInfo = Objects.requireNonNull(shopInfo, "shopInfo");

        this.predicates = List.copyOf(predicates);
        this.successInteractors = List.copyOf(successInteractors);
        this.failureInteractors = List.copyOf(failureInteractors);
        this.displays = List.copyOf(displays);
    }

    public @NotNull Point mapOrigin() {
        return mapOrigin;
    }

    public @NotNull @Unmodifiable List<ShopPredicate> predicates() {
        return predicates;
    }

    public @NotNull @Unmodifiable List<ShopInteractor> successInteractors() {
        return successInteractors;
    }

    public @NotNull @Unmodifiable List<ShopInteractor> failureInteractors() {
        return failureInteractors;
    }

    public @NotNull @Unmodifiable List<ShopDisplay> displays() {
        return displays;
    }

    public @NotNull Instance instance() {
        return instance;
    }

    public @NotNull ShopInfo info() {
        return shopInfo;
    }

    public void initialize() {
        for (ShopDisplay display : displays) {
            display.initialize(this);
        }

        for (ShopInteractor interactor : successInteractors) {
            interactor.initialize(this);
        }

        for (ShopInteractor interactor : failureInteractors) {
            interactor.initialize(this);
        }
    }

    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        boolean success = shopInfo.predicateEvaluation().evaluate(predicates, interaction);
        List<ShopInteractor> interactorsToCall = success ? successInteractors : failureInteractors;

        for (ShopInteractor interactor : interactorsToCall) {
            success &= interactor.handleInteraction(interaction);
        }

        for (ShopDisplay display : displays) {
            display.update(this, interaction, success);
        }
    }

    @Override
    public void tick(long time) {
        for (ShopDisplay display : displays) {
            display.tick(time);
        }

        for (ShopInteractor interactor : successInteractors) {
            interactor.tick(time);
        }

        for (ShopInteractor interactor : failureInteractors) {
            interactor.tick(time);
        }
    }
}
