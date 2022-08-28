package com.github.phantazmnetwork.zombies.game.map.shop;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.game.map.PositionalMapObject;
import com.github.phantazmnetwork.zombies.game.map.shop.display.ShopDisplay;
import com.github.phantazmnetwork.zombies.game.map.shop.interactor.ShopInteractor;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.ShopPredicate;
import com.github.phantazmnetwork.zombies.map.Evaluation;
import com.github.phantazmnetwork.zombies.map.ShopInfo;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
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
        boolean interact = callPredicates(data.predicateEvaluation(), interaction);
        List<ShopInteractor> interactorsToCall = interact ? successInteractors : failureInteractors;

        for (ShopInteractor interactor : interactorsToCall) {
            interactor.handleInteraction(interaction);
        }

        for (ShopDisplay display : displays) {
            display.update(this, interaction, interact);
        }
    }

    private boolean callPredicates(Evaluation evaluation, PlayerInteraction interaction) {
        switch (evaluation) {
            case ALL_TRUE -> {
                for (ShopPredicate predicate : predicates) {
                    if (!predicate.canInteract(interaction)) {
                        return false;
                    }
                }

                return true;
            }
            case ANY_TRUE -> {
                boolean foundTrue = false;
                for (ShopPredicate predicate : predicates) {
                    if (predicate.canInteract(interaction)) {
                        foundTrue = true;
                        break;
                    }
                }

                return foundTrue;
            }
            default -> throw new IllegalStateException();
        }
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
