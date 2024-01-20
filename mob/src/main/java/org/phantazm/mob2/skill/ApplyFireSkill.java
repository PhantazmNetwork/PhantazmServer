package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.TagUtils;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Model("mob.skill.apply_fire")
@Cache
public class ApplyFireSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;


    @FactoryMethod
    public ApplyFireSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = data;
        this.selector = selector;
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Impl(mob, selector.apply(mob, injectionStore), data);
    }

    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @NotNull @ChildPath("selector") String selector) {
        @Default("trigger")
        public static @NotNull ConfigElement defaultTrigger() {
            return ConfigPrimitive.NULL;
        }
    }

    private static final class Impl extends TargetedSkill {
        private static final Tag<Integer> FIRE_ID_TAG = Tag.Integer(TagUtils.uniqueTagName());
        private static final AtomicInteger FIRE_ID = new AtomicInteger();

        private final Data data;
        private final Set<Entity> entities;
        private final int fireId;

        public Impl(@NotNull Mob self, @NotNull Selector selector, @NotNull Data data) {
            super(self, selector);
            this.entities = Collections.newSetFromMap(new WeakHashMap<>());
            this.data = data;

            this.fireId = FIRE_ID.getAndIncrement();
        }

        @Override
        protected void useOnTarget(@NotNull Target target) {
            self.getAcquirable().sync(self -> {
                target.forType(Entity.class, entity -> {
                    entity.setTag(FIRE_ID_TAG, fireId);
                    entity.setOnFire(true);

                    entities.add(entity);
                });
            });
        }

        @Override
        public void end() {
            self.getAcquirable().sync(self -> {
                for (Entity entity : entities) {
                    entity.getAcquirable().sync(target -> {
                        if (!target.isRemoved() && entity.getTag(FIRE_ID_TAG) == fireId) {
                            target.setOnFire(false);
                        }
                    });
                }

                entities.clear();
            });
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
