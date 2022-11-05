package com.github.phantazmnetwork.zombies.game.powerup;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.powerup.deactivation_predicate.timed")
public class TimedPredicate implements DeactivationPredicate {
    private final Data data;
    private long start = -1;

    @FactoryMethod
    public TimedPredicate(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
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

    @DataObject
    public record Data(long time) {
        
    }
}
