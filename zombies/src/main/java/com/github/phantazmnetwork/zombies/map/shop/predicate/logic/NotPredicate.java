package com.github.phantazmnetwork.zombies.map.shop.predicate.logic;

import com.github.phantazmnetwork.zombies.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.map.shop.predicate.PredicateBase;
import com.github.phantazmnetwork.zombies.map.shop.predicate.ShopPredicate;
import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.shop.predicate.not")
public class NotPredicate extends PredicateBase<NotPredicate.Data> {
    private final ShopPredicate predicate;

    @FactoryMethod
    public NotPredicate(@NotNull Data data, @DataName("predicate") ShopPredicate predicate) {
        super(data);
        this.predicate = Objects.requireNonNull(predicate, "predicate");
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        return !predicate.canInteract(interaction);
    }

    @DataObject
    public record Data(@NotNull @DataPath("predicate") String predicate) {
    }
}