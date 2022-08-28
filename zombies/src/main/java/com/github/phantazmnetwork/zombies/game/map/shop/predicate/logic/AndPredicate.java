package com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic;

import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.PredicateBase;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.ShopPredicate;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Model("zombies.map.shop.predicate.and")
public class AndPredicate extends PredicateBase<AndPredicate.Data> {
    @ProcessorMethod
    public static ConfigProcessor<AndPredicate.Data> processor() {
        return new BooleanOperatorDataProcessor<>(Data::new);
    }

    private final List<ShopPredicate> predicates;

    @FactoryMethod
    public AndPredicate(@NotNull Data data, @DataName("predicates") List<ShopPredicate> predicates) {
        super(data);
        this.predicates = List.copyOf(predicates);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        for (ShopPredicate predicate : predicates) {
            if (!predicate.canInteract(interaction)) {
                return false;
            }
        }

        return true;
    }

    @DataObject
    public record Data(@DataPath("predicates") List<String> paths) implements BooleanOperatorData {
    }
}
