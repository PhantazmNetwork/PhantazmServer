package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.core.Target;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.goal.GoalCreator;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;
import org.phantazm.proxima.bindings.minestom.goal.GoalGroup;
import org.phantazm.proxima.bindings.minestom.goal.ProximaGoal;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Model("mob.skill.apply_ai_goal")
@Cache
public class ApplyAIGoalSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;
    private final GoalCreator goalCreator;

    @FactoryMethod
    public ApplyAIGoalSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector,
        @NotNull @Child("goal") GoalCreator goalCreator) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
        this.goalCreator = Objects.requireNonNull(goalCreator);
    }

    @Override
    public @NotNull Skill get() {
        return new Internal(ExtensionHolder.requestKey(Extension.class), selector.get(), goalCreator, data);
    }

    @Default("""
        {
          trigger=null,
          group=0,
          index=0
        }
        """)
    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        int group,
        int index,
        @NotNull String appliedTag) {
    }

    private static class Extension {
        record Entry(Reference<Mob> mob,
            Reference<ProximaGoal> goal) {
        }

        private final List<Entry> entries;

        private Extension() {
            this.entries = new CopyOnWriteArrayList<>();
        }
    }

    private static class Internal extends TargetedSkill {
        private final ExtensionHolder.Key<Extension> key;
        private final Tag<Boolean> applied;
        private final Data data;
        private final GoalCreator goalCreator;

        private Internal(ExtensionHolder.Key<Extension> key, Selector selector, GoalCreator goalCreator, Data data) {
            super(selector);
            this.key = key;
            this.applied = Tag.Boolean(data.appliedTag).defaultValue(false);
            this.data = data;
            this.goalCreator = goalCreator;
        }

        @Override
        public void init(@NotNull Mob mob) {
            mob.extensions().set(key, new Extension());
        }

        @Override
        protected void useOnTarget(@NotNull Target target, @NotNull Mob mob) {
            Extension ext = mob.extensions().get(key);
            target.forType(Mob.class, targetMob -> {
                List<GoalGroup> goalGroups = targetMob.goalGroups();
                if (data.group < 0 || data.group >= goalGroups.size()) return;
                if (targetMob.tagHandler().getAndUpdateTag(applied, ignored -> true)) return;

                ProximaGoal goal = goalCreator.create(targetMob);
                goalGroups.get(data.group).addGoal(data.index, goal);

                ext.entries.add(new Extension.Entry(new WeakReference<>(targetMob), new WeakReference<>(goal)));
            });
        }

        @Override
        public void end(@NotNull Mob mob) {
            Extension ext = mob.extensions().get(key);
            for (Extension.Entry entry : ext.entries) {
                Mob entryMob = entry.mob.get();
                ProximaGoal entryGoal = entry.goal.get();

                if (entryMob != null) entryMob.tagHandler().removeTag(applied);
                if (entryMob == null || entryGoal == null) continue;

                List<GoalGroup> goalGroups = entryMob.goalGroups();
                if (data.group < 0 || data.group >= goalGroups.size()) return;

                goalGroups.get(data.group).removeGoal(entryGoal);
            }
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
