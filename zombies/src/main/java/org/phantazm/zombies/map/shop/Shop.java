package org.phantazm.zombies.map.shop;

import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.Vec3I;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.Tickable;
import org.phantazm.core.VecUtils;
import org.phantazm.core.tracker.Bounded;
import org.phantazm.zombies.map.ShopInfo;
import org.phantazm.zombies.map.shop.display.ShopDisplay;
import org.phantazm.zombies.map.shop.interactor.ShopInteractor;
import org.phantazm.zombies.map.shop.predicate.ShopPredicate;

import java.util.List;
import java.util.Objects;

public class Shop implements Tickable, Bounded {
    private final Point mapOrigin;
    private final Point center;
    private final List<Bounds3I> bounds;

    private final Instance instance;
    private final ShopInfo shopInfo;

    private final List<ShopPredicate> predicates;
    private final List<ShopInteractor> successInteractors;
    private final List<ShopInteractor> failureInteractors;
    private final List<ShopDisplay> displays;

    public Shop(@NotNull Point mapOrigin, @NotNull ShopInfo shopInfo, @NotNull Instance instance,
            @NotNull List<ShopPredicate> predicates, @NotNull List<ShopInteractor> successInteractors,
            @NotNull List<ShopInteractor> failureInteractors, @NotNull List<ShopDisplay> displays) {
        this.mapOrigin = Objects.requireNonNull(mapOrigin, "mapOrigin");

        Vec3I triggerLocation = shopInfo.triggerLocation();
        this.center = mapOrigin.add(triggerLocation.x(), triggerLocation.y(), triggerLocation.z()).add(0.5);
        this.bounds = List.of(Bounds3I.immutable(mapOrigin.blockX() + triggerLocation.x(),
                mapOrigin.blockY() + triggerLocation.y(), mapOrigin.blockZ() + triggerLocation.z(), 1, 1, 1));

        this.instance = Objects.requireNonNull(instance, "instance");
        this.shopInfo = Objects.requireNonNull(shopInfo, "shopInfo");

        this.predicates = List.copyOf(predicates);
        this.successInteractors = List.copyOf(successInteractors);
        this.failureInteractors = List.copyOf(failureInteractors);
        this.displays = List.copyOf(displays);
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
        boolean interact = shopInfo.predicateEvaluation().evaluate(predicates, interaction);
        List<ShopInteractor> interactorsToCall = interact ? successInteractors : failureInteractors;

        for (ShopInteractor interactor : interactorsToCall) {
            interactor.handleInteraction(interaction);
        }

        for (ShopDisplay display : displays) {
            display.update(this, interaction, interact);
        }
    }

    public @NotNull Point computeAbsolutePosition(@NotNull Point offset) {
        Vec3I location = shopInfo.triggerLocation().add(mapOrigin.blockX(), mapOrigin.blockY(), mapOrigin.blockZ());
        return VecUtils.toVec(location).add(0.5).add(offset);
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

    @Override
    public @NotNull @Unmodifiable List<Bounds3I> bounds() {
        return bounds;
    }

    @Override
    public @NotNull Point center() {
        return center;
    }
}
