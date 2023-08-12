package org.phantazm.zombies.powerup.predicate;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.scene.ZombiesScene;

@Model("zombies.powerup.deactivation_predicate.timed")
public class TimedDeactivationPredicate implements DeactivationPredicateComponent {
    private final Data data;

    @FactoryMethod
    public TimedDeactivationPredicate(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull DeactivationPredicate apply(@NotNull ZombiesScene scene) {
        return new Predicate(data);
    }

    @DataObject
    public record Data(long time) {

    }

    private static class Predicate implements DeactivationPredicate {
        private final Data data;
        private long startTicks = -1;

        private Predicate(Data data) {
            this.data = data;
        }

        @Override
        public void activate(long time) {
            startTicks = 0;
        }

        @Override
        public boolean shouldDeactivate(long time) {
            if (startTicks < 0) {
                return false;
            }

            return ++startTicks >= data.time;
        }
    }
}
