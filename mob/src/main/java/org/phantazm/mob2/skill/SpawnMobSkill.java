package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
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
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.core.TagUtils;
import org.phantazm.mob2.*;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.*;

@Model("mob.skill.spawn_mob")
@Cache
public class SpawnMobSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;
    private final SpawnCallbackComponent callback;
    private final Tag<Integer> nameCounterTag;

    private static class Extension {
        private final Mob self;

        private Extension(Mob self) {
            this.self = self;
        }

        private TagHandler ownerTags;

        private Mob findRootOwner() {
            Mob current = self;
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

        private TagHandler ownerTags(boolean unlimitedSpawns, boolean useLocalCount) {
            if (ownerTags != null) {
                return ownerTags;
            }

            return ownerTags = unlimitedSpawns ? null : (useLocalCount ? self : findRootOwner()).tagHandler();
        }
    }

    @FactoryMethod
    public SpawnMobSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector,
        @NotNull @Child("callback") SpawnCallbackComponent callback) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
        this.callback = Objects.requireNonNull(callback);
        this.nameCounterTag = Tag.Integer(TagUtils.uniqueTagName()).defaultValue(0);
    }

    @Override
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(selector.apply(holder), data, holder.requestKey(Extension.class), callback.apply(holder),
            nameCounterTag);
    }

    @Default("""
        {
          trigger=null,
          useLocalCount=false
        }
        """)
    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @NotNull Key identifier,
        int spawnAmount,
        int maxSpawn,
        boolean useLocalCount) {

        private boolean unlimitedSpawns() {
            return maxSpawn < 0;
        }
    }

    private static class Internal extends TargetedSkill {
        private final Data data;
        private final ExtensionHolder.Key<Extension> key;
        private final SpawnCallback callback;
        private final Tag<Integer> spawnCountTag;

        private Internal(Selector selector, Data data, ExtensionHolder.Key<Extension> key, SpawnCallback callback,
            Tag<Integer> spawnCountTag) {
            super(selector);
            this.data = data;
            this.key = key;
            this.callback = callback;
            this.spawnCountTag = spawnCountTag;
        }

        @Override
        public void init(@NotNull Mob mob) {
            mob.extensions().set(key, new Extension(mob));
        }

        @Override
        protected void useOnTarget(@NotNull Target target, @NotNull Mob mob) {
            Instance instance = mob.getInstance();
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

            spawn(mob, instance, points);
        }

        private void spawn(Mob self, Instance instance, Collection<? extends Point> points) {
            if (data.unlimitedSpawns()) {
                spawnAt(self, instance, points);
                return;
            }

            Extension ext = self.extensions().get(key);
            if (ext.ownerTags(data.unlimitedSpawns(), data.useLocalCount).getTag(spawnCountTag) >= data.maxSpawn) {
                return;
            }

            List<Point> spawnTargets = new ArrayList<>(points.size() * data.spawnAmount);
            ext.ownerTags(data.unlimitedSpawns(), data.useLocalCount).updateTag(spawnCountTag, currentAmount -> {
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

            spawnAt(self, instance, spawnTargets);
        }

        private void spawnAt(Mob self, Instance instance, Collection<? extends Point> targets) {
            MobSpawner mobSpawner = self.extensions().get(BasicMobSpawner.SPAWNER_KEY);
            for (Point point : targets) {
                callback.accept(mobSpawner.spawn(data.identifier, instance, Pos.fromPoint(point), newMob -> {
                    setup(self, newMob);
                }));
            }
        }

        private void setup(Mob self, Mob child) {
            child.setOwner(self.getUuid());
            if (data.unlimitedSpawns()) {
                return;
            }

            child.addSkill(new Skill() {
                @Override
                public void end(@NotNull Mob mob) {
                    Extension ext = self.extensions().get(key);
                    ext.ownerTags(data.unlimitedSpawns(), data.useLocalCount).updateTag(spawnCountTag, value -> value - 1);
                }
            });
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
