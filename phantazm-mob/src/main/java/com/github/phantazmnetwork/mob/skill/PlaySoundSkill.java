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

public class PlaySoundSkill implements Skill {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "skill.play_sound");
    
    private final TargetSelector<? extends Audience> selectorCreator;

    private final Sound sound;

    private final boolean followAudience;

    public PlaySoundSkill(@NotNull TargetSelector<? extends Audience> selectorCreator, @NotNull Sound sound,
                          boolean followAudience) {
        this.selectorCreator = Objects.requireNonNull(selectorCreator, "selectorCreator");
        this.sound = Objects.requireNonNull(sound, "sound");
        this.followAudience = followAudience;
    }

    public @NotNull TargetSelector<? extends Audience> getSelectorCreator() {
        return selectorCreator;
    }

    public @NotNull Sound getSound() {
        return sound;
    }

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
