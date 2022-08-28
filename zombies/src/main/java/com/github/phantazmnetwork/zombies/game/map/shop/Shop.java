package com.github.phantazmnetwork.zombies.game.map.shop;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.game.map.PositionalMapObject;
import com.github.phantazmnetwork.zombies.game.map.shop.display.ShopDisplay;
import com.github.phantazmnetwork.zombies.game.map.shop.interactor.ShopInteractor;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.ShopPredicate;
import com.github.phantazmnetwork.zombies.map.ShopInfo;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Shop extends PositionalMapObject<ShopInfo> implements Tickable {
    private final List<ShopPredicate> predicates;
    private final List<ShopInteractor> successInteractors;
    private final List<ShopInteractor> failureInteractors;
    private final List<ShopDisplay> displays;

    public Shop(@NotNull ShopInfo info, @NotNull Instance instance, @NotNull List<ShopPredicate> predicates,
            @NotNull List<ShopInteractor> successInteractors, @NotNull List<ShopInteractor> failureInteractors,
            @NotNull List<ShopDisplay> displays) {
        super(info, info.triggerLocation(), instance);

        this.predicates = List.copyOf(predicates);
        this.successInteractors = List.copyOf(successInteractors);
        this.failureInteractors = List.copyOf(failureInteractors);
        this.displays = List.copyOf(displays);
    }

    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        boolean interact = data.predicateEvaluation().evaluate(predicates, interaction);
        List<ShopInteractor> interactorsToCall = interact ? successInteractors : failureInteractors;

        for (ShopInteractor interactor : interactorsToCall) {
            interactor.handleInteraction(interaction);
        }

        for (ShopDisplay display : displays) {
            display.update(this, interaction, interact);
        }
    }

    public @NotNull Vec3D computeAbsolutePosition(@NotNull Vec3D offset) {
        Vec3I location = data.triggerLocation();
        return Vec3D.of(location.getX() + 0.5 + offset.getX(), location.getY() + 0.5 + offset.getY(),
                location.getZ() + 0.5 + offset.getZ());
    }

    @Override
    public void tick(long time) {
        for (ShopInteractor interactor : successInteractors) {
            interactor.tick(time);
        }

        for (ShopDisplay display : displays) {
            display.tick(time);
        }
    }
}
