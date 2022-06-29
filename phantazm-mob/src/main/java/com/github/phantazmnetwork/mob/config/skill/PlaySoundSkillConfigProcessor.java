package com.github.phantazmnetwork.mob.config.skill;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
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

    private static final ConfigProcessor<Sound> SOUND_PROCESSOR = AdventureConfigProcessors.sound();

    private final ConfigProcessor<TargetSelector<? extends Audience>> selectorProcessor;

    public PlaySoundSkillConfigProcessor(@NotNull ConfigProcessor<TargetSelector<? extends Audience>> selectorProcessor) {
        this.selectorProcessor = Objects.requireNonNull(selectorProcessor, "selectorProcessor");
    }

    @Override
    public PlaySoundSkill dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        ConfigElement selectorElement = element.getElementOrThrow("audienceSelector");
        TargetSelector<? extends Audience> selector = selectorProcessor.dataFromElement(selectorElement);
        Sound sound = SOUND_PROCESSOR.dataFromElement(element.getElementOrThrow("sound"));
        boolean followAudience = element.getBooleanOrThrow("followAudience");

        return new PlaySoundSkill(selector, sound, followAudience);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull PlaySoundSkill playSoundSkill) throws ConfigProcessException {
        ConfigNode node = new LinkedConfigNode(3);

        TargetSelector<? extends Audience> selector = playSoundSkill.getSelectorCreator();
        node.put("audienceSelector", selectorProcessor.elementFromData(selector));
        node.put("sound", SOUND_PROCESSOR.elementFromData(playSoundSkill.getSound()));
        node.put("followAudience", new ConfigPrimitive(playSoundSkill.shouldFollowAudience()));

        return node;
    }

}
