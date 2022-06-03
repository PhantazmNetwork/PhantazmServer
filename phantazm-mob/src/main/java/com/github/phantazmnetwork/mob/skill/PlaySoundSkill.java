package com.github.phantazmnetwork.mob.skill;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlaySoundSkill implements Skill, VariantSerializable {

    public final static String SERIAL_NAME = "playSoundSkill";
    
    private final TargetSelector<? extends Audience> audienceSelector;

    private final Sound sound;

    private final boolean followAudience;

    public PlaySoundSkill(@NotNull TargetSelector<? extends Audience> audienceSelector, @NotNull Sound sound,
                          boolean followAudience) {
        this.audienceSelector = Objects.requireNonNull(audienceSelector, "audienceSelector");
        this.sound = Objects.requireNonNull(sound, "sound");
        this.followAudience = followAudience;
    }

    public @NotNull TargetSelector<? extends Audience> getAudienceSelector() {
        return audienceSelector;
    }

    public @NotNull Sound getSound() {
        return sound;
    }

    public boolean shouldFollowAudience() {
        return followAudience;
    }

    @Override
    public void use(@NotNull PhantazmMob sender) {
        audienceSelector.selectTarget(sender).ifPresent(audience -> {
            if (followAudience) {
                audience.playSound(sound, Sound.Emitter.self());
            }
            else {
                audience.playSound(sound);
            }
        });
    }

    @Override
    public @NotNull String getSerialName() {
        return SERIAL_NAME;
    }
}
