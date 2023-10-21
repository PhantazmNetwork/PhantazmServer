package org.phantazm.zombies.powerup.predicate;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.scene2.ZombiesScene;

@Model("zombies.powerup.pickup_predicate.always")
@Cache
public class AlwaysPickupPredicate implements PickupPredicateComponent {
    public static final PickupPredicate INSTANCE = zombiesPlayer -> true;

    @FactoryMethod
    public AlwaysPickupPredicate() {
    }

    @Override
    public @NotNull PickupPredicate apply(@NotNull ZombiesScene scene) {
        return INSTANCE;
    }
}
