package org.phantazm.zombies.powerup.predicate;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.OptionalInt;

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
        public void activate(@NotNull Powerup powerup, @Nullable ZombiesPlayer zombiesPlayer, long time) {

        }

        @Override
        public boolean shouldDeactivate(long time) {
            return true;
        }

        @Override
        public @NotNull OptionalInt duration(@NotNull Powerup powerup, @Nullable ZombiesPlayer zombiesPlayer) {
            return OptionalInt.empty();
        }
    }
}
