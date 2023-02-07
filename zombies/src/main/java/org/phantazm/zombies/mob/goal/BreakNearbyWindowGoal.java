package org.phantazm.zombies.mob.goal;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.goal.GoalCreator;
import org.phantazm.proxima.bindings.minestom.ProximaEntity;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;
import org.phantazm.zombies.event.EntityBreakWindowEvent;
import org.phantazm.zombies.map.Window;
import org.phantazm.zombies.map.objects.MapObjects;

import java.util.Objects;
import java.util.function.Supplier;


@Model("zombies.mob.goal.break_window")
@Cache(false)
public class BreakNearbyWindowGoal implements GoalCreator {
    private final Data data;
    private final Supplier<? extends MapObjects> mapObjects;

    @FactoryMethod
    public BreakNearbyWindowGoal(@NotNull Data data, @NotNull Supplier<? extends MapObjects> mapObjects) {
        this.data = Objects.requireNonNull(data, "data");
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
    }

    @Override
    public @NotNull ProximaGoal create(@NotNull PhantazmMob mob) {
        return new Goal(data, mapObjects.get().windowTracker(), mob.entity());
    }

    private static final class Goal implements ProximaGoal {
        private final Data data;
        private final BoundedTracker<Window> windowTracker;
        private final ProximaEntity self;

        private long lastBreakCheck;

        private Goal(Data data, BoundedTracker<Window> windowTracker, ProximaEntity self) {
            this.data = data;
            this.self = self;
            this.windowTracker = windowTracker;
            this.lastBreakCheck = -1;
        }

        @Override
        public void start() {
            
        }

        @Override
        public void tick(long time) {
            if (lastBreakCheck == -1) {
                lastBreakCheck = time;
                return;
            }

            long elapsedTicks = (time - lastBreakCheck) / MinecraftServer.TICK_MS;
            if (elapsedTicks > data.breakTicks) {
                windowTracker.closestInRange(self.getPosition().add(0, self.getBoundingBox().height() / 2, 0),
                        data.breakRadius).ifPresent(window -> {
                    window.setLastBreakTime(time);

                    int index = window.getIndex();
                    int targetIndex = index - data.breakCount;

                    int amount = window.updateIndex(targetIndex);
                    if (amount != 0) {
                        EventDispatcher.call(new EntityBreakWindowEvent(self, window, -amount));
                    }
                });

                lastBreakCheck = time;
            }
        }

        @Override
        public void end() {
            this.lastBreakCheck = -1;
        }

        @Override
        public boolean shouldStart() {
            return true;
        }

        @Override
        public boolean shouldEnd() {
            return false;
        }
    }

    @DataObject
    public record Data(int breakTicks, int breakCount, double breakRadius) {
    }
}
