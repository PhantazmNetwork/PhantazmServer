package org.phantazm.zombies.map.shop.predicate.logic;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.predicate.PredicateBase;
import org.phantazm.zombies.map.shop.predicate.ShopPredicate;

import java.util.List;

@Model("zombies.map.shop.predicate.and")
public class AndPredicate extends PredicateBase<AndPredicate.Data> {
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
    public record Data(@NotNull @DataPath("predicates") List<String> paths) {
    }
}
