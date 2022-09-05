package com.github.phantazmnetwork.mob.skill;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.mob.target.TargetSelector;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link Skill} that plays a {@link Sound}.
 */
@Model("mob.skill.play_sound")
public class PlaySoundSkill implements Skill {

    @DataObject
    public record Data(@NotNull @DataPath("selector") String selectorPath,
                       @NotNull Sound sound,
                       boolean followAudience) {

        public Data {
            Objects.requireNonNull(selectorPath, "selectorPath");
            Objects.requireNonNull(sound, "sound");
        }

    }

    private final Data data;

    private final TargetSelector<? extends Audience> selector;

    /**
     * Creates a {@link PlaySoundSkill}.
     *
     * @param selector The {@link TargetSelector} used to select {@link Audience}s
     */
    @FactoryMethod
    public PlaySoundSkill(@NotNull Data data,
            @NotNull @DataName("selector") TargetSelector<? extends Audience> selector) {
        this.data = Objects.requireNonNull(data, "data");
        this.selector = Objects.requireNonNull(selector, "selector");
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Sound> soundProcessor = ConfigProcessors.sound();
        return new ConfigProcessor<>() {
            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                String selectorPath = element.getStringOrThrow("selectorPath");
                Sound sound = soundProcessor.dataFromElement(element.getElementOrThrow("sound"));
                boolean followAudience = element.getBooleanOrThrow("followAudience");

                return new Data(selectorPath, sound, followAudience);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                return ConfigNode.of("selectorPath", data.selectorPath(), "sound",
                        soundProcessor.elementFromData(data.sound()), "followAudience", data.followAudience());
            }
        };
    }

    @Override
    public void use() {
        selector.selectTarget().ifPresent(audience -> {
            if (data.followAudience()) {
                audience.playSound(data.sound(), Sound.Emitter.self());
            }
            else {
                audience.playSound(data.sound());
            }
        });
    }

}
