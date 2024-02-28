package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Trigger;

import java.util.Objects;

@Model("mob.skill.timer")
@Cache
public class TimerSkill implements SkillComponent {
    private final Data data;
    private final SkillComponent delegate;

    @FactoryMethod
    public TimerSkill(@NotNull Data data, @NotNull @Child("delegate") SkillComponent delegate) {
        this.data = Objects.requireNonNull(data);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(data, holder.requestKey(TimerSkillAbstract.Extension.class),
            delegate.apply(holder));
    }

    @Default("""
        {
            trigger=null,
            repeat=-1,
            requiresActivation=false,
            resetOnActivation=true,
            exceedMobLifetime=false
        }
        """)
    @DataObject
    public record Data(@Nullable Trigger trigger,
        @NotNull @ChildPath("delegate") String delegate,
        int repeat,
        int interval,
        boolean requiresActivation,
        boolean resetOnActivation,
        boolean exceedMobLifetime) {
    }

    private static class Internal extends TimerSkillAbstract {
        private final Data data;
        private final int interval;

        public Internal(Data data, ExtensionHolder.Key<Extension> key, Skill delegate) {
            super(key, delegate, data.requiresActivation, data.resetOnActivation, data.repeat, data.interval,
                !data.exceedMobLifetime);
            this.data = data;
            this.interval = data.interval;
        }

        @Override
        public int computeInterval() {
            return interval;
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
