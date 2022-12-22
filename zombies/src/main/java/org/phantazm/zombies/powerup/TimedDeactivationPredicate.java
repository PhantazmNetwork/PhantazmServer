package org.phantazm.zombies.powerup;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.powerup.deactivation_predicate.timed")
public class TimedDeactivationPredicate implements Supplier<DeactivationPredicate> {
    private final Data data;

    @FactoryMethod
    public TimedDeactivationPredicate(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public DeactivationPredicate get() {
        return new Predicate(data);
    }

    @DataObject
    public record Data(long time) {

    }

    private static class Predicate implements DeactivationPredicate {
        private final Data data;
        private long start = -1;

        private Predicate(Data data) {
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

            return (time - start) / MinecraftServer.TICK_MS >= data.time;
        }
    }
}
