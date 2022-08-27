package com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic;

import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.PredicateBase;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.ShopPredicate;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.predicate.xor")
public class XorPredicate extends PredicateBase<XorPredicate.Data> {
    @ProcessorMethod
    public static ConfigProcessor<XorPredicate.Data> processor() {
        return new OperatorDataProcessor<>(Data::new);
    }

    private final List<ShopPredicate> predicates;

    @FactoryMethod
    public XorPredicate(@NotNull Data data, @DataName("predicates") List<ShopPredicate> predicates) {
        super(data);

        predicates.sort(Comparator.reverseOrder());
        this.predicates = List.copyOf(predicates);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        boolean hasPreviousResult = false;
        boolean result = false;

        for (ShopPredicate predicate : predicates) {
            boolean thisResult = predicate.canInteract(interaction);
            result = (hasPreviousResult && result) ^ thisResult;
            hasPreviousResult = true;
        }

        return result;
    }

    @DataObject
    public record Data(int priority, @DataPath("predicates") List<String> paths) implements OperatorData {
    }
}
