package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.target.TargetSelector;

import java.util.Objects;
import java.util.Random;

/**
 * A {@link Skill} that plays a {@link Sound}.
 */
@Model("mob.skill.play_sound")
@Cache(false)
public class PlaySoundSkill implements Skill {
    private final Data data;
    private final TargetSelector<Object> selector;
    private final Random random;

    /**
     * Creates a {@link PlaySoundSkill}.
     *
     * @param selector The {@link TargetSelector} used to select {@link Audience}s
     */
    @FactoryMethod
    public PlaySoundSkill(@NotNull Data data, @NotNull @Child("selector") TargetSelector<Object> selector) {
        this.data = Objects.requireNonNull(data, "data");
        this.selector = Objects.requireNonNull(selector, "selector");
        this.random = new Random();
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        selector.selectTarget(self).ifPresent(object -> {
            if (object instanceof Iterable<?> iterable) {
                for (Object playerObject : iterable) {
                    if (!(playerObject instanceof Player player)) {
                        continue;
                    }

                    Instance instance = player.getInstance();
                    if (instance == null) {
                        continue;
                    }

                    if (data.followAudience) {
                        player.playSound(randomize(), player.getPosition());
                    }
                    else {
                        instance.playSound(randomize(), self.entity().getPosition());
                    }
                }
            }
            else {
                Audience target = (Audience)object;
                if (data.followAudience) {
                    target.playSound(randomize(), Sound.Emitter.self());
                }
                else {
                    Instance instance = self.entity().getInstance();
                    if (instance != null) {
                        instance.playSound(randomize(), Sound.Emitter.self());
                    }
                }
            }
        });
    }

    private Sound randomize() {
        return Sound.sound(data.sound).seed(random.nextLong()).build();
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector, @NotNull Sound sound, boolean followAudience) {
        @Default("followAudience")
        public static @NotNull ConfigElement defaultFollowAudience() {
            return ConfigPrimitive.of(false);
        }
    }
}
