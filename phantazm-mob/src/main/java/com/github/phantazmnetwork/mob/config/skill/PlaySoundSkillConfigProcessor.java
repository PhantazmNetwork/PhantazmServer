package com.github.phantazmnetwork.mob.config.skill;

import com.github.phantazmnetwork.mob.skill.PlaySoundSkill;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlaySoundSkillConfigProcessor implements ConfigProcessor<PlaySoundSkill> {

    private final ConfigProcessor<TargetSelector<Audience>> audienceSelectorConfigProcessor;

    private final ConfigProcessor<Sound> soundConfigProcessor;

    public PlaySoundSkillConfigProcessor(@NotNull ConfigProcessor<TargetSelector<Audience>> audienceSelectorConfigProcessor,
                                         @NotNull ConfigProcessor<Sound> soundConfigProcessor) {
        this.audienceSelectorConfigProcessor = Objects.requireNonNull(audienceSelectorConfigProcessor,
                "audienceSelectorConfigProcessor");
        this.soundConfigProcessor = Objects.requireNonNull(soundConfigProcessor, "soundConfigProcessor");
    }

    @Override
    public PlaySoundSkill dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        TargetSelector<Audience> audienceSelector = audienceSelectorConfigProcessor.dataFromElement(element.getElement("audienceSelector"));
        Sound sound = soundConfigProcessor.dataFromElement(element.getElement("sound"));

        return null;
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull PlaySoundSkill playSoundSkill) throws ConfigProcessException {
        ConfigNode node = new LinkedConfigNode();
        //node.put("audienceSelector", audienceSelectorConfigProcessor.elementFromData((TargetSelector<Audience>) playSoundSkill.getAudienceSelector()));
        node.put("sound", soundConfigProcessor.elementFromData(playSoundSkill.getSound()));
        node.put("followAudience", new ConfigPrimitive(playSoundSkill.shouldFollowAudience()));

        return node;
    }
}
