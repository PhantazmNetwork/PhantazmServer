package org.phantazm.zombies.powerup2.predicate;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.function.Supplier;

@Model("zombies.powerup.deactivation_predicate.immediate")
@Cache
public class ImmediateDeactivationPredicate implements DeactivationPredicateComponent {
    public static final DeactivationPredicate INSTANCE = new Predicate();

    @FactoryMethod
    public ImmediateDeactivationPredicate() {
    }

    @Override
    public @NotNull DeactivationPredicate apply(@NotNull ZombiesScene scene) {
        return INSTANCE;
    }

    private static class Predicate implements DeactivationPredicate {

        @Override
        public void activate(long time) {

        }

        @Override
        public boolean shouldDeactivate(long time) {
            return true;
        }
    }
}
