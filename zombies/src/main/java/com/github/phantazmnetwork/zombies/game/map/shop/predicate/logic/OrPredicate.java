package com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic;

import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.PredicateBase;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.ShopPredicate;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

@Model("zombies.map.shop.predicate.or")
public class OrPredicate extends PredicateBase<OrPredicate.Data> {
    @ProcessorMethod
    public static ConfigProcessor<OrPredicate.Data> processor() {
        return new OperatorDataProcessor<>(Data::new);
    }

    private final List<ShopPredicate> predicates;

    @FactoryMethod
    public OrPredicate(@NotNull Data data, @DataName("predicates") List<ShopPredicate> predicates) {
        super(data);

        predicates.sort(Comparator.reverseOrder());
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
    public record Data(int priority, @DataPath("predicates") List<String> paths) implements OperatorData {
    }
}
