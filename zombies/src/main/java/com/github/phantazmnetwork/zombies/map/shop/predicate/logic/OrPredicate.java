package com.github.phantazmnetwork.zombies.map.shop.predicate.logic;

import com.github.phantazmnetwork.zombies.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.map.shop.predicate.PredicateBase;
import com.github.phantazmnetwork.zombies.map.shop.predicate.ShopPredicate;
import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Model("zombies.map.shop.predicate.or")
public class OrPredicate extends PredicateBase<OrPredicate.Data> {
    private final List<ShopPredicate> predicates;

    @FactoryMethod
    public OrPredicate(@NotNull Data data, @DataName("predicates") List<ShopPredicate> predicates) {
        super(data);
        this.predicates = List.copyOf(predicates);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        for (ShopPredicate predicate : predicates) {
            if (predicate.canInteract(interaction)) {
                return true;
            }
        }

        return false;
    }

    @DataObject
    public record Data(@NotNull @DataPath("predicates") List<String> paths) {
    }
}
