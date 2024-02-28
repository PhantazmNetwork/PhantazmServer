package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.core.TagUtils;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Model("mob.skill.apply_fire")
@Cache
public class ApplyFireSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    private static class Extension {
        private static final AtomicInteger FIRE_ID = new AtomicInteger();

        private final Set<Entity> entities = Collections.newSetFromMap(new WeakHashMap<>());
        private final int fireId = FIRE_ID.getAndIncrement();
    }

    @FactoryMethod
    public ApplyFireSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = data;
        this.selector = selector;
    }

    @Override
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(selector.apply(holder), holder.requestKey(Extension.class), data);
    }

    @Default("""
        {
          trigger=null
        }
        """)
    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @NotNull @ChildPath("selector") String selector) {
    }

    private static final class Internal extends TargetedSkill {
        private static final Tag<Integer> FIRE_ID_TAG = Tag.Integer(TagUtils.uniqueTagName());

        private final ExtensionHolder.Key<Extension> key;
        private final Data data;

        public Internal(@NotNull Selector selector, @NotNull ExtensionHolder.Key<Extension> key, @NotNull Data data) {
            super(selector);
            this.key = key;
            this.data = data;
        }

        @Override
        public void init(@NotNull Mob mob) {
            mob.extensions().set(key, new Extension());
        }

        @Override
        protected void useOnTarget(@NotNull Target target, @NotNull Mob mob) {
            Extension ext = mob.extensions().get(key);

            mob.getAcquirable().sync(self -> {
                target.forType(Entity.class, entity -> {
                    entity.setTag(FIRE_ID_TAG, ext.fireId);
                    entity.setOnFire(true);

                    ext.entities.add(entity);
                });
            });
        }

        @Override
        public void end(@NotNull Mob mob) {
            Extension ext = mob.extensions().get(key);

            mob.getAcquirable().sync(self -> {
                for (Entity entity : ext.entities) {
                    entity.getAcquirable().sync(target -> {
                        Integer tag = entity.getTag(FIRE_ID_TAG);
                        if (!target.isRemoved() && tag != null && tag == ext.fireId) {
                            target.setOnFire(false);
                        }
                    });
                }

                ext.entities.clear();
            });
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
