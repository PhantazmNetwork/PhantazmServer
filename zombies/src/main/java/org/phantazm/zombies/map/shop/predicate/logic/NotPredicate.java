package org.phantazm.zombies.map.shop.predicate.logic;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.predicate.PredicateBase;
import org.phantazm.zombies.map.shop.predicate.ShopPredicate;

import java.util.Objects;

@Model("zombies.map.shop.predicate.not")
@Cache(false)
public class NotPredicate extends PredicateBase<NotPredicate.Data> {
    private final ShopPredicate predicate;

    @FactoryMethod
    public NotPredicate(@NotNull Data data, @Child("predicate") ShopPredicate predicate) {
        super(data);
        this.predicate = Objects.requireNonNull(predicate);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction, @NotNull Shop shop) {
        return !predicate.canInteract(interaction, shop);
    }

    @DataObject
    public record Data(@NotNull @ChildPath("predicate") String predicate) {
    }
}
