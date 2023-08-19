package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;

public class PlaySoundSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent targetSelector;
    private final Random random;

    @FactoryMethod
    public PlaySoundSkill(@NotNull Data data, @NotNull @Child("target") SelectorComponent targetSelector) {
        this.data = Objects.requireNonNull(data);
        this.targetSelector = Objects.requireNonNull(targetSelector);
        this.random = new Random();
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(mob, targetSelector.apply(mob, injectionStore), data, random);
    }

    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @ChildPath("target") String selector,
        @NotNull Sound sound,
        boolean broadcast) {
        @Default("trigger")
        public static @NotNull ConfigElement defaultTrigger() {
            return ConfigPrimitive.NULL;
        }

        @Default("broadcast")
        public static @NotNull ConfigElement defaultBroadcast() {
            return ConfigPrimitive.of(true);
        }
    }

    private static class Internal extends TargetedSkill {
        private final Data data;
        private final Random random;

        private Internal(Mob self, Selector selector, Data data, Random random) {
            super(self, selector);
            this.data = data;
            this.random = random;
        }

        private Sound randomize(Sound sound) {
            return Sound.sound(sound).seed(random.nextLong()).build();
        }

        @Override
        protected void useOnTarget(@NotNull Target target) {
            Instance instance = self.getInstance();
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
