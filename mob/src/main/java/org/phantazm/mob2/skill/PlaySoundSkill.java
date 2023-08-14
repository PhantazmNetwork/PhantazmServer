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
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Keys;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.Collection;

public class PlaySoundSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent targetSelector;

    @FactoryMethod
    public PlaySoundSkill(@NotNull Data data, @NotNull @Child("target") SelectorComponent targetSelector) {
        this.data = data;
        this.targetSelector = targetSelector;
    }

    @Override
    public @NotNull Skill apply(@NotNull InjectionStore injectionStore) {
        return new Internal(injectionStore.get(Keys.MOB_KEY), targetSelector.apply(injectionStore), data);
    }

    @DataObject
    public record Data(@ChildPath("target") String selector, @NotNull Sound sound, boolean broadcast) {
        @Default("broadcast")
        public static @NotNull ConfigElement defaultBroadcast() {
            return ConfigPrimitive.of(true);
        }
    }

    private static class Internal extends TargetedSkill {
        private final Data data;

        private Internal(Mob self, Selector selector, Data data) {
            super(self, selector);
            this.data = data;
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
                        player.playSound(data.sound, entity.getPosition());
                    }
                    else {
                        instance.playSound(data.sound, entity.getPosition());
                    }
                }

                return;
            }

            for (Point point : target.locations()) {
                instance.playSound(data.sound, point);
            }
        }
    }
}
