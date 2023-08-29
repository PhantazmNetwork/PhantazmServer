package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.*;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.Collection;
import java.util.Objects;

@Model("mob.skill.spawn_mob")
@Cache
public class SpawnMobSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;
    private final SpawnCallbackComponent callback;

    @FactoryMethod
    public SpawnMobSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector,
        @NotNull @Child("callback") SpawnCallbackComponent callback) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
        this.callback = Objects.requireNonNull(callback);
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(mob, selector.apply(mob, injectionStore), data, injectionStore.get(Keys.MOB_SPAWNER),
            callback.apply(mob, injectionStore));
    }

    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @NotNull @ChildPath("selector") String selector,
        @NotNull @ChildPath("callback") String callback,
        @NotNull Key identifier,
        int spawnAmount,
        int maxSpawn) {
        @Default("trigger")
        public static @NotNull ConfigElement defaultTrigger() {
            return ConfigPrimitive.NULL;
        }
    }

    private static class Internal extends TargetedSkill {
        private final Data data;
        private final MobSpawner mobSpawner;
        private final SpawnCallback callback;

        private int spawnCount;

        private final Object lock;

        private Internal(Mob self, Selector selector, Data data, MobSpawner mobSpawner, SpawnCallback callback) {
            super(self, selector);
            this.data = data;
            this.mobSpawner = mobSpawner;
            this.callback = callback;

            this.lock = new Object();
        }

        @Override
        protected void useOnTarget(@NotNull Target target) {
            Instance instance = self.getInstance();
            if (instance == null) {
                return;
            }

            if (data.spawnAmount <= 0 || data.maxSpawn == 0) {
                return;
            }

            Collection<? extends Point> points = target.locations();
            if (points.isEmpty()) {
                return;
            }

            boolean unlimited = data.maxSpawn < 0;
            if (!unlimited) {
                synchronized (lock) {
                    spawn(false, instance, points);
                }

                return;
            }

            spawn(true, instance, points);
        }

        private void spawn(boolean unlimited, Instance instance, Iterable<? extends Point> points) {
            if (!unlimited && spawnCount >= data.maxSpawn) {
                return;
            }

            for (int i = 0; i < data.spawnAmount; i++) {
                for (Point point : points) {
                    Mob mob = mobSpawner.spawn(data.identifier, instance, Pos.fromPoint(point), self -> {
                        if (unlimited) {
                            return;
                        }

                        self.addSkill(new Skill() {
                            @Override
                            public void end() {
                                synchronized (lock) {
                                    spawnCount--;
                                }
                            }
                        });
                    });

                    callback.accept(mob);
                    if (!unlimited && ++spawnCount >= data.maxSpawn) {
                        return;
                    }
                }
            }
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
