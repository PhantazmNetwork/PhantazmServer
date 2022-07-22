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
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Shop extends PositionalMapObject<ShopInfo> implements Tickable {
    private final List<ShopPredicate> predicates;
    private final List<ShopInteractor> interactors;
    private final List<ShopDisplay> displays;

    public Shop(@NotNull ShopInfo info, @NotNull Vec3I origin, @NotNull Instance instance,
                @NotNull List<ShopPredicate> predicates, @NotNull List<ShopInteractor> interactors,
                @NotNull List<ShopDisplay> displays) {
        super(info, origin, instance);

        predicates.sort(Comparator.reverseOrder());
        interactors.sort(Comparator.reverseOrder());

        this.predicates = List.copyOf(predicates);
        this.interactors = List.copyOf(interactors);
        this.displays = List.copyOf(displays);
    }

    public void handleInteraction(@NotNull PlayerInteraction interaction) {
        boolean interact = callPredicates(data.predicateEvaluation(), interaction);
        if (interact) {
            for (ShopInteractor interactor : interactors) {
                interactor.handleInteraction(interaction);
            }
        }

        for (ShopDisplay display : displays) {
            display.update(this, interaction, interact);
        }
    }

    private boolean callPredicates(Evaluation evaluation, PlayerInteraction interaction) {
        switch (evaluation) {
            case ALL_TRUE -> {
                for (ShopPredicate predicate : predicates) {
                    if (!predicate.canHandleInteraction(interaction)) {
                        return false;
                    }
                }

                return true;
            }
            case ANY_TRUE -> {
                boolean foundTrue = false;
                for (ShopPredicate predicate : predicates) {
                    if (predicate.canHandleInteraction(interaction)) {
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

    }
}
