package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;

@Model("mob.skill.play_sound")
@Cache
public class PlaySoundSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent targetSelector;
    private final Random random;

    @FactoryMethod
    public PlaySoundSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent targetSelector) {
        this.data = Objects.requireNonNull(data);
        this.targetSelector = Objects.requireNonNull(targetSelector);
        this.random = new Random();
    }

    @Override
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(targetSelector.apply(holder), data, random);
    }

    @Default("""
        {
          trigger=null,
          broadcast=true
        }
        """)
    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @ChildPath("selector") String selector,
        @NotNull Sound sound,
        boolean broadcast) {
    }

    private static class Internal extends TargetedSkill {
        private final Data data;
        private final Random random;

        private Internal(Selector selector, Data data, Random random) {
            super(selector);
            this.data = data;
            this.random = random;
        }

        private Sound randomize(Sound sound) {
            return Sound.sound(sound).seed(random.nextLong()).build();
        }

        @Override
        protected void useOnTarget(@NotNull Target target, @NotNull Mob mob) {
            Instance instance = mob.getInstance();
            if (instance == null) {
                return;
            }

            Collection<? extends Entity> entities = target.targets();
            if (!entities.isEmpty()) {
                for (Entity entity : entities) {
                    if (!data.broadcast && entity instanceof Player player) {
                        player.playSound(randomize(data.sound), entity.getPosition());
                    } else {
                        instance.playSound(randomize(data.sound), entity.getPosition());
                    }
                }

                return;
            }

            for (Point point : target.locations()) {
                instance.playSound(randomize(data.sound), point);
            }
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
