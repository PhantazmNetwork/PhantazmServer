package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.audience.AudienceProvider;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * A {@link GunEffect} that plays a {@link Sound}.
 */
public class PlaySoundEffect implements GunEffect {

    private final Data data;
    private final AudienceProvider audienceProvider;

    /**
     * Creates a {@link PlaySoundEffect}.
     *
     * @param data             The {@link Data} for this {@link PlaySoundEffect}
     * @param audienceProvider The {@link AudienceProvider} for this {@link PlaySoundEffect}
     */
    public PlaySoundEffect(@NotNull Data data, @NotNull AudienceProvider audienceProvider) {
        this.data = Objects.requireNonNull(data, "data");
        this.audienceProvider = Objects.requireNonNull(audienceProvider, "audienceProvider");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();
        ConfigProcessor<Sound> soundProcessor = AdventureConfigProcessors.sound();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key audienceProviderKey =
                        keyProcessor.dataFromElement(element.getElementOrThrow("audienceProviderKey"));
                Sound sound = soundProcessor.dataFromElement(element.getElementOrThrow("sound"));

                return new Data(audienceProviderKey, sound);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.put("audienceProviderKey", keyProcessor.elementFromData(data.audienceProviderKey));
                node.put("sound", soundProcessor.elementFromData(data.sound()));

                return node;
            }
        };
    }

    /**
     * Creates a dependency consumer for {@link Data}s.
     *
     * @return A dependency consumer for {@link Data}s
     */
    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> keys.add(data.audienceProviderKey());
    }

    @Override
    public void apply(@NotNull GunState state) {
        audienceProvider.provideAudience()
                        .ifPresent(audience -> audience.playSound(data.sound(), Sound.Emitter.self()));
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    /**
     * Data for an {@link PlaySoundEffect}.
     *
     * @param audienceProviderKey A {@link Key} to the {@link PlaySoundEffect}'s {@link AudienceProvider}
     * @param sound               The {@link Sound} to play
     */
    public record Data(@NotNull Key audienceProviderKey, @NotNull Sound sound) implements Keyed {

        /**
         * The serial {@link Key} for this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.play_sound");

        /**
         * Creates a {@link Data}.
         *
         * @param audienceProviderKey A {@link Key} to the {@link PlaySoundEffect}'s {@link AudienceProvider}
         * @param sound               The {@link Sound} to play
         */
        public Data {
            Objects.requireNonNull(audienceProviderKey, "audienceProviderKey");
            Objects.requireNonNull(sound, "sound");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

}
