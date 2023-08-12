package org.phantazm.zombies.powerup.predicate;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.MinecraftServer;
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
        private int startTick = -1;

        private Predicate(Data data) {
            this.data = data;
        }

        @Override
        public void activate(long time) {
            startTick = MinecraftServer.currentTick();
        }

        @Override
        public boolean shouldDeactivate(long time) {
            if (startTick < 0) {
                return false;
            }

            return MinecraftServer.currentTick() - startTick >= data.time;
        }
    }
}
