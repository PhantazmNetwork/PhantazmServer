package org.phantazm.zombies.map.shop.predicate;

import com.github.steanky.element.core.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;
import org.phantazm.zombies.map.shop.interactor.ShopInteractor;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.predicate.interacting")
@Cache(false)
public class InteractingPredicate implements ShopPredicate {
    private final ShopPredicate delegate;
    private final List<ShopInteractor> interactors;

    @FactoryMethod
    public InteractingPredicate(@NotNull @Child("delegate") ShopPredicate delegate,
            @NotNull @Child("interactors") List<ShopInteractor> interactors) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.interactors = List.copyOf(interactors);
    }

    @Override
    public boolean canInteract(@NotNull PlayerInteraction interaction, @NotNull Shop shop) {
        if (delegate.canInteract(interaction, shop)) {
            for (ShopInteractor interactor : interactors) {
                interactor.handleInteraction(interaction);
            }

            return true;
        }

        return false;
    }

    @DataObject
    public record Data(@NotNull @ChildPath("delegate") String delegatePath,
                       @NotNull @ChildPath("interactors") List<String> interactorPaths) {
    }
}
