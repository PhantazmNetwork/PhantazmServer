package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.core.Target;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Model("mob.skill.apply_boolean_tag")
@Cache
public class ApplyBooleanTagSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public ApplyBooleanTagSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public @NotNull Skill get() {
        return new Internal(ExtensionHolder.requestKey(Extension.class), selector.get(), data);
    }

    private static class Extension {
        private final List<Reference<Entity>> targets;

        private Extension() {
            this.targets = new CopyOnWriteArrayList<>();
        }
    }

    @Default("""
        {
          trigger=null,
        }
        """)
    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @NotNull String tag) {
    }

    private static class Internal extends TargetedSkill {
        private final ExtensionHolder.Key<Extension> key;
        private final Data data;
        private final Tag<Boolean> tag;

        private Internal(ExtensionHolder.Key<Extension> key, Selector selector, Data data) {
            super(selector);
            this.key = key;
            this.data = data;
            this.tag = Tag.Boolean(data.tag).defaultValue(false);
        }

        @Override
        protected void useOnTarget(@NotNull Target target, @NotNull Mob mob) {
            Extension ext = mob.extensions().get(key);
            target.forType(Entity.class, entity -> {
                entity.setTag(tag, true);
                ext.targets.add(new WeakReference<>(entity));
            });
        }

        @Override
        public void end(@NotNull Mob mob) {
            Extension ext = mob.extensions().get(key);
            for (Reference<Entity> reference : ext.targets) {
                Entity entity = reference.get();
                if (entity == null) continue;

                entity.removeTag(tag);
            }
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
