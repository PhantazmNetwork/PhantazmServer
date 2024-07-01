package org.phantazm.zombies.map.shop.predicate.logic;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.predicate.PredicateBase;
import org.phantazm.zombies.map.shop.predicate.ShopPredicate;

import java.util.List;

@Model("zombies.map.shop.predicate.or")
@Cache(false)
public class OrPredicate extends PredicateBase<OrPredicate.Data> {
    private final List<ShopPredicate> predicates;

    @FactoryMethod
    public OrPredicate(@NotNull Data data, @Child("paths") List<ShopPredicate> predicates) {
        super(data);
        this.predicates = List.copyOf(predicates);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction, @NotNull Shop shop) {
        boolean succeeded = false;
        for (ShopPredicate predicate : predicates) {
            if (predicate.canInteract(interaction, shop)) {
                succeeded = true;

                if (data.shortCircuit) {
                    return true;
                }
            }
        }

        return succeeded;
    }

    @Default("""
        {
          shortCircuit=true
        }
        """)
    @DataObject
    public record Data(boolean shortCircuit) {

    }
}
