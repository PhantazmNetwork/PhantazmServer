package com.github.phantazmnetwork.zombies.powerup;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.powerup.deactivation_predicate.timed")
public class TimedPredicateBuilder implements Supplier<DeactivationPredicate> {
    private final Data data;

    @FactoryMethod
    public TimedPredicateBuilder(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public DeactivationPredicate get() {
        return new TimedPredicate(data);
    }

    @DataObject
    public record Data(long time) {

    }

    private static class TimedPredicate implements DeactivationPredicate {
        private final Data data;
        private long start = -1;

        private TimedPredicate(Data data) {
            this.data = data;
        }

        @Override
        public void activate(long time) {
            start = time;
        }

        @Override
        public boolean shouldDeactivate(long time) {
            if (start < 0) {
                return false;
            }

            return time - start >= data.time;
        }
    }
}
