package org.phantazm.zombies.mob2.goal;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.goal.GoalCreator;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;
import org.phantazm.zombies.event.mob.MobBreakWindowEvent;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.map.Window;
import org.phantazm.zombies.map.ZombiesMap;
import org.phantazm.zombies.mob2.InjectionKeys;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;
import java.util.Optional;

@Model("zombies.mob.goal.break_window")
@Cache
public class BreakNearbyWindowGoal implements GoalCreator {
    private final Data data;

    @FactoryMethod
    public BreakNearbyWindowGoal(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull ProximaGoal create(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        ZombiesScene scene = injectionStore.get(InjectionKeys.SCENE);
        ZombiesMap map = scene.map();
        return new Goal(data, map.windowHandler().tracker(), map.objects().roomTracker(), mob, scene);
    }

    @DataObject
    public record Data(int breakTicks,
        int breakCount,
        double breakRadius) {
    }

    private static final class Goal implements ProximaGoal {
        private final Data data;
        private final BoundedTracker<Window> windowTracker;
        private final BoundedTracker<Room> roomTracker;
        private final Mob self;
        private final ZombiesScene scene;

        private long breakTicks = -1;

        private Goal(Data data, BoundedTracker<Window> windowTracker, BoundedTracker<Room> roomTracker,
            Mob self, ZombiesScene scene) {
            this.data = data;
            this.windowTracker = windowTracker;
            this.roomTracker = roomTracker;
            this.self = self;
            this.scene = scene;
        }

        @Override
        public void tick(long time) {
            ++breakTicks;

            LivingEntity entity = self;
            if (breakTicks > data.breakTicks) {
                Optional<Room> roomOptional = roomTracker.atPoint(entity.getPosition());
                if (roomOptional.isPresent()) {
                    return;
                }

                BoundingBox boundingBox = entity.getBoundingBox();

                windowTracker.closestInRangeToBounds(entity.getPosition(), boundingBox.width(), boundingBox.height(),
                    data.breakRadius).ifPresent(window -> {
                    int index = window.getIndex();
                    int targetIndex = index - data.breakCount;

                    int amount = window.updateIndex(targetIndex);
                    if (amount != 0) {
                        scene.broadcastEvent(new MobBreakWindowEvent(self, window, -amount));
                        entity.swingMainHand();
                    }
                });

                breakTicks = 0;
            }
        }

        @Override
        public void end() {
            this.breakTicks = -1;
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
}
