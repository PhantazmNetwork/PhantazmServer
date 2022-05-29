package com.github.phantazmnetwork.mob.skill;

import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.LinearComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlaySoundSkill implements Skill {

    private final TargetSelector<? extends Audience> audienceSelector;

    private final Sound sound;

    private final boolean followAudience;

    public PlaySoundSkill(@NotNull TargetSelector<? extends Audience> audienceSelector, @NotNull Sound sound,
                          boolean followAudience) {
        this.audienceSelector = Objects.requireNonNull(audienceSelector, "audienceSelector");
        this.sound = Objects.requireNonNull(sound, "sound");
        this.followAudience = followAudience;
    }

    @Override
    public void use(@NotNull PhantazmMob<?> sender) {
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
    public @NotNull String getSerialType() {
        return "playSoundSkill";
    }
}
