package com.github.phantazmnetwork.mob.config.skill;

import com.github.phantazmnetwork.api.config.VariantSerializable;
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

import java.util.Map;
import java.util.Objects;

public class PlaySoundSkillConfigProcessor implements ConfigProcessor<PlaySoundSkill> {

    private final Map<String, ConfigProcessor<TargetSelector<? extends Audience>>> processors;

    private final ConfigProcessor<Sound> soundConfigProcessor;

    public PlaySoundSkillConfigProcessor(@NotNull Map<String, ConfigProcessor<TargetSelector<? extends Audience>>> processors,
                                         @NotNull ConfigProcessor<Sound> soundConfigProcessor) {
        this.processors = Objects.requireNonNull(processors, "processors");
        this.soundConfigProcessor = Objects.requireNonNull(soundConfigProcessor, "soundConfigProcessor");
    }

    @Override
    public PlaySoundSkill dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        ConfigElement selectorElement = element.getElementOrThrow("audienceSelector");
        ConfigProcessor<TargetSelector<? extends Audience>> selectorProcessor = processors.get(selectorElement.getStringOrThrow("serialName"));
        TargetSelector<? extends Audience> selector = selectorProcessor.dataFromElement(selectorElement);
        Sound sound = soundConfigProcessor.dataFromElement(element.getElementOrThrow("sound"));
        boolean followAudience = element.getBooleanOrThrow("followAudience");

        return new PlaySoundSkill(selector, sound, followAudience);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull PlaySoundSkill playSoundSkill) throws ConfigProcessException {
        ConfigNode node = new LinkedConfigNode();

        TargetSelector<? extends Audience> selector = playSoundSkill.getAudienceSelector();
        if (!(selector instanceof VariantSerializable serializable)) {
            throw new ConfigProcessException("cannot serialize target selector");
        }
        node.put("audienceSelector", processors.get(serializable.getSerialKey()).elementFromData(selector));
        node.put("sound", soundConfigProcessor.elementFromData(playSoundSkill.getSound()));
        node.put("followAudience", new ConfigPrimitive(playSoundSkill.shouldFollowAudience()));

        return node;
    }
}
