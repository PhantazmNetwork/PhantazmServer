package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.*;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Model("mob.skill.spawn_mob")
@Cache
public class SpawnMobSkill implements SkillComponent {
    private static final String NAME_PREFIX = "spawn_mob_";
    private static final AtomicInteger NAME_COUNTER = new AtomicInteger();

    private final Data data;
    private final SelectorComponent selector;
    private final SpawnCallbackComponent callback;
    private final Tag<Integer> nameCounterTag;

    @FactoryMethod
    public SpawnMobSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector,
        @NotNull @Child("callback") SpawnCallbackComponent callback) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
        this.callback = Objects.requireNonNull(callback);
        this.nameCounterTag = Tag.Integer(NAME_PREFIX + NAME_COUNTER.getAndIncrement())
            .defaultValue(0);
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(mob, selector.apply(mob, injectionStore), data, injectionStore.get(Keys.MOB_SPAWNER),
            callback.apply(mob, injectionStore), nameCounterTag);
    }

    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @NotNull @ChildPath("selector") String selector,
        @NotNull @ChildPath("callback") String callback,
        @NotNull Key identifier,
        int spawnAmount,
        int maxSpawn,
        boolean useLocalCount) {
        @Default("trigger")
        public static @NotNull ConfigElement defaultTrigger() {
            return ConfigPrimitive.NULL;
        }

        @Default("useLocalCount")
        public static @NotNull ConfigElement defaultUseLocalCount() {
            return ConfigPrimitive.of(false);
        }
    }

    private static class Internal extends TargetedSkill {
        private final Data data;
        private final MobSpawner mobSpawner;
        private final SpawnCallback callback;
        private final Tag<Integer> spawnCountTag;
        private final TagHandler ownerTags;

        private Internal(Mob self, Selector selector, Data data, MobSpawner mobSpawner, SpawnCallback callback,
            Tag<Integer> spawnCountTag) {
            super(self, selector);
            this.data = data;
            this.mobSpawner = mobSpawner;
            this.callback = callback;
            this.spawnCountTag = spawnCountTag;
            this.ownerTags = data.maxSpawn < 0 ? null : (data.useLocalCount ? self : findRootOwner()).tagHandler();
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
                spawn(false, instance, points);
                return;
            }

            spawn(true, instance, points);
        }

        private Mob findRootOwner() {
            Mob current = this.self;
            while (true) {
                UUID uuid = current.getOwner();
                if (uuid == null) {
                    break;
                }

                if (!(Entity.getEntity(uuid) instanceof Mob parentMob)) {
                    break;
                }

                current = parentMob;
            }

            return current;
        }

        private void spawn(boolean unlimited, Instance instance, Collection<? extends Point> points) {
            if (unlimited) {
                spawnAt(true, instance, points);
                return;
            }

            if (ownerTags.getTag(spawnCountTag) >= data.maxSpawn) {
                return;
            }

            List<Point> spawnTargets = new ArrayList<>(points.size() * data.spawnAmount);
            ownerTags.updateTag(spawnCountTag, currentAmount -> {
                if (currentAmount >= data.maxSpawn) {
                    return currentAmount;
                }

                for (int i = 0; i < data.spawnAmount; i++) {
                    for (Point point : points) {
                        spawnTargets.add(point);
                        if (++currentAmount >= data.maxSpawn) {
                            return currentAmount;
                        }
                    }
                }

                return currentAmount;
            });

            spawnAt(false, instance, spawnTargets);
        }

        private void spawnAt(boolean unlimited, Instance instance, Collection<? extends Point> points) {
            for (Point point : points) {
                Mob mob = mobSpawner.spawn(data.identifier, instance, Pos.fromPoint(point), self -> {
                    self.setOwner(super.self.getUuid());
                    if (unlimited) {
                        return;
                    }

                    self.addSkill(new Skill() {
                        @Override
                        public void end() {
                            ownerTags.updateTag(spawnCountTag, value -> value - 1);
                        }
                    });
                });

                callback.accept(mob);
            }
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
