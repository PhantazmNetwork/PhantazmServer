package org.phantazm.zombies.map.shop.predicate.logic;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.predicate.PredicateBase;
import org.phantazm.zombies.map.shop.predicate.ShopPredicate;

import java.util.List;

@Model("zombies.map.shop.predicate.or")
public class OrPredicate extends PredicateBase<OrPredicate.Data> {
    private final List<ShopPredicate> predicates;

    @FactoryMethod
    public OrPredicate(@NotNull Data data, @Child("predicates") List<ShopPredicate> predicates) {
        super(data);
        this.predicates = List.copyOf(predicates);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction) {
        boolean succeeded = false;
        for (ShopPredicate predicate : predicates) {
            if (predicate.canInteract(interaction)) {
                succeeded = true;

                if (data.shortCircuit) {
                    return true;
                }
            }
        }

        return succeeded;
    }

    @DataObject
    public record Data(boolean shortCircuit, @NotNull @ChildPath("predicates") List<String> paths) {
        @Default("shortCircuit")
        public static @NotNull ConfigElement shortCircuitDefault() {
            return ConfigPrimitive.of(true);
        }
    }
}
