package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class SpawnMobSkill implements SkillComponent {
    public interface SpawnCallbackComponent extends Function<@NotNull InjectionStore, @NotNull SpawnCallback> {

    }

    public interface SpawnCallback extends Consumer<@NotNull Mob> {
    }

    public static class NoCallback implements SpawnCallbackComponent {
        private static final SpawnCallback INSTANCE = mob -> {
        };

        @FactoryMethod
        public NoCallback() {
        }

        @Override
        public @NotNull SpawnCallback apply(@NotNull InjectionStore injectionStore) {
            return INSTANCE;
        }
    }

    private final Data data;
    private final SelectorComponent selector;
    private final SpawnCallback callback;

    @FactoryMethod
    public SpawnMobSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector,
            @NotNull @Child("callback") SpawnCallback callback) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
        this.callback = Objects.requireNonNull(callback);
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(mob, selector.apply(mob, injectionStore), data, injectionStore.get(Keys.MOB_SPAWNER),
                callback);
    }

    @DataObject
    public record Data(@Nullable Trigger trigger,
                       @NotNull @ChildPath("selector") String selector,
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

        private final Object lock = new Object();

        private int spawnCount;

        private Internal(Mob self, Selector selector, Data data, MobSpawner mobSpawner, SpawnCallback callback) {
            super(self, selector);
            this.data = data;
            this.mobSpawner = mobSpawner;
            this.callback = callback;
        }

        @Override
        protected void useOnTarget(@NotNull Target target) {
            Instance instance = self.getInstance();
            if (instance == null) {
                return;
            }

            Collection<? extends Point> points = target.locations();
            if (points.isEmpty() || data.spawnAmount <= 0 || data.maxSpawn == 0) {
                return;
            }

            List<Mob> spawnList = new ArrayList<>(points.size());

            boolean unlimited = data.maxSpawn < 0;
            synchronized (lock) {
                outer:
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

                        spawnList.add(mob);
                        if (!unlimited && ++spawnCount >= data.maxSpawn) {
                            break outer;
                        }
                    }
                }
            }

            for (Mob mob : spawnList) {
                callback.accept(mob);
            }
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
