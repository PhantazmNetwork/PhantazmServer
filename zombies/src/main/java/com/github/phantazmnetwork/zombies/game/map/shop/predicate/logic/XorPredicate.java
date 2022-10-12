package com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic;

import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.PredicateBase;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.ShopPredicate;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Model("zombies.map.shop.predicate.xor")
public class XorPredicate extends PredicateBase<XorPredicate.Data> {
    private final List<ShopPredicate> predicates;

    @FactoryMethod
    public XorPredicate(@NotNull Data data, @DataName("predicates") List<ShopPredicate> predicates) {
        super(data);
        this.predicates = List.copyOf(predicates);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        boolean result = false;
        for (ShopPredicate predicate : predicates) {
            result = result ^ predicate.canInteract(interaction);
        }

        return result;
    }

    @DataObject
    public record Data(@NotNull @DataPath("predicates") List<String> paths) {
    }
}
