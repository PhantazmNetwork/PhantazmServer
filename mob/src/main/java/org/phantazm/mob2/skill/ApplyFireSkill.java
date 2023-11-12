package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.*;

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
        private final Data data;
        private final Set<Entity> entities;
        private final Object lock;

        public Impl(@NotNull Mob self, @NotNull Selector selector, @NotNull Data data) {
            super(self, selector);
            this.entities = Collections.newSetFromMap(new WeakHashMap<>());
            this.data = data;
            this.lock = new Object();
        }

        @Override
        protected void useOnTarget(@NotNull Target target) {
            synchronized (lock) {
                target.forType(Entity.class, entity -> {
                    entity.setOnFire(true);
                    entities.add(entity);
                });
            }
        }

        @Override
        public void end() {
            synchronized (lock) {
                Iterator<Entity> iterator = entities.iterator();
                while (iterator.hasNext()) {
                    Entity entity = iterator.next();
                    if (entity.isRemoved()) {
                        iterator.remove();
                        continue;
                    }

                    entity.setOnFire(false);
                }
            }
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
