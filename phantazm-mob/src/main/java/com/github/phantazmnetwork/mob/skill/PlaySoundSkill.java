package com.github.phantazmnetwork.mob.skill;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.target.TargetSelectorInstance;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link Skill} that plays a {@link Sound}.
 */
public class PlaySoundSkill implements Skill {

    /**
     * The serial {@link Key} for {@link PlaySoundSkill}s.
     */
    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "skill.play_sound");
    
    private final TargetSelector<? extends Audience> selectorCreator;

    private final Sound sound;

    private final boolean followAudience;

    /**
     * Creates a {@link PlaySoundSkill}.
     * @param selector The {@link TargetSelector} used to select {@link Audience}s
     * @param sound The {@link Sound} to play
     * @param followAudience Whether the {@link Sound} should follow the {@link Audience}
     */
    public PlaySoundSkill(@NotNull TargetSelector<? extends Audience> selector, @NotNull Sound sound,
                          boolean followAudience) {
        this.selectorCreator = Objects.requireNonNull(selector, "selectorCreator");
        this.sound = Objects.requireNonNull(sound, "sound");
        this.followAudience = followAudience;
    }

    /**
     * Gets the {@link TargetSelector} used to select {@link Audience}s.
     * @return The {@link TargetSelector} used to select {@link Audience}s
     */
    public @NotNull TargetSelector<? extends Audience> getSelector() {
        return selectorCreator;
    }

    /**
     * Gets the {@link Sound} to play.
     * @return The {@link Sound} to play
     */
    public @NotNull Sound getSound() {
        return sound;
    }

    /**
     * Gets whether the {@link Sound} should follow the {@link Audience}.
     * @return Whether the {@link Sound} should follow the {@link Audience}
     */
    public boolean shouldFollowAudience() {
        return followAudience;
    }

    @Override
    public @NotNull SkillInstance createSkill(@NotNull PhantazmMob sender) {
        TargetSelectorInstance<? extends Audience> selector = selectorCreator.createSelector(sender);
        return () -> {
            selector.selectTarget().ifPresent(audience -> {
                if (followAudience) {
                    audience.playSound(sound, Sound.Emitter.self());
                } else {
                    audience.playSound(sound);
                }
            });
        };
    }

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
