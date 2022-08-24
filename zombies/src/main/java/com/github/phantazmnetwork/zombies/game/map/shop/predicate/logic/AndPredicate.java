package com.github.phantazmnetwork.zombies.game.map.shop.predicate.logic;

import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.PredicateBase;
import com.github.phantazmnetwork.zombies.game.map.shop.predicate.ShopPredicate;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.predicate.and")
public class AndPredicate extends PredicateBase<AndPredicate.Data> {
    @ProcessorMethod
    public static ConfigProcessor<AndPredicate.Data> processor() {
        return new BinaryOperatorProcessor<>(Data::new);
    }

    private final ShopPredicate first;
    private final ShopPredicate second;

    @FactoryMethod
    public AndPredicate(@NotNull Data data, @DataName("first") ShopPredicate first,
            @DataName("second") ShopPredicate second) {
        super(data);
        this.first = Objects.requireNonNull(first, "first");
        this.second = Objects.requireNonNull(second, "second");
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return first.canInteract(interaction) && second.canInteract(interaction);
    }

    @DataObject
    public record Data(int priority, @DataPath("first") String firstPath, @DataPath("second") String secondPath)
            implements BinaryOperatorData {
    }
}
