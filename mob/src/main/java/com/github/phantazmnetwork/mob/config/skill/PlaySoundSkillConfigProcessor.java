package com.github.phantazmnetwork.mob.config.skill;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.mob.skill.PlaySoundSkill;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link ConfigProcessor} for {@link PlaySoundSkill}s.
 */
public class PlaySoundSkillConfigProcessor implements ConfigProcessor<PlaySoundSkill> {

    private static final ConfigProcessor<Sound> SOUND_PROCESSOR = ConfigProcessors.sound();

    private final ConfigProcessor<TargetSelector<? extends Audience>> selectorProcessor;

    /**
     * Creates a new {@link PlaySoundSkillConfigProcessor}.
     *
     * @param selectorProcessor A {@link ConfigProcessor} for audience {@link TargetSelector}s
     */
    public PlaySoundSkillConfigProcessor(
            @NotNull ConfigProcessor<TargetSelector<? extends Audience>> selectorProcessor) {
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
    public @NotNull ConfigElement elementFromData(@NotNull PlaySoundSkill playSoundSkill)
            throws ConfigProcessException {
        ConfigNode node = new LinkedConfigNode(3);

        TargetSelector<? extends Audience> selector = playSoundSkill.getSelector();
        node.put("audienceSelector", selectorProcessor.elementFromData(selector));
        node.put("sound", SOUND_PROCESSOR.elementFromData(playSoundSkill.getSound()));
        node.putBoolean("followAudience", playSoundSkill.shouldFollowAudience());

        return node;
    }

}
