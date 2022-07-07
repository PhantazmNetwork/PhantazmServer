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

public class PlaySoundEffect implements GunEffect {

    public record Data(@NotNull Key audienceProviderKey, @NotNull Sound sound) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.play_sound");

        public Data {
            Objects.requireNonNull(audienceProviderKey, "audienceProviderKey");
            Objects.requireNonNull(sound, "sound");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();
        ConfigProcessor<Sound> soundProcessor = AdventureConfigProcessors.sound();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key audienceProviderKey = keyProcessor.dataFromElement(element.getElementOrThrow("audienceProviderKey"));
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

    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> keys.add(data.audienceProviderKey());
    }

    private final Data data;

    private final AudienceProvider audienceProvider;

    public PlaySoundEffect(@NotNull Data data, @NotNull AudienceProvider audienceProvider) {
        this.data = Objects.requireNonNull(data, "data");
        this.audienceProvider = Objects.requireNonNull(audienceProvider, "audienceProvider");
    }

    @Override
    public void apply(@NotNull GunState state) {
        audienceProvider.provideAudience().ifPresent(audience -> audience.playSound(data.sound(),
                Sound.Emitter.self()));
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

}
