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
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * A {@link GunEffect} that sends a message to an {@link Audience}.
 */
public class SendMessageEffect implements GunEffect {

    private final Data data;
    private final AudienceProvider audienceProvider;

    /**
     * Creates a {@link SendMessageEffect}.
     *
     * @param data             The {@link SendMessageEffect}'s {@link Data}
     * @param audienceProvider The {@link SendMessageEffect}'s {@link AudienceProvider}
     */
    public SendMessageEffect(@NotNull Data data, @NotNull AudienceProvider audienceProvider) {
        this.data = Objects.requireNonNull(data, "data");
        this.audienceProvider = Objects.requireNonNull(audienceProvider, "audienceProvider");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Component> componentProcessor = AdventureConfigProcessors.component();
        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key audienceProviderKey =
                        keyProcessor.dataFromElement(element.getElementOrThrow("audienceProviderKey"));
                Component message = componentProcessor.dataFromElement(element.getElementOrThrow("message"));

                return new Data(audienceProviderKey, message);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.put("audienceProviderKey", keyProcessor.elementFromData(data.audienceProviderKey()));
                node.put("message", componentProcessor.elementFromData(data.message()));

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
        audienceProvider.provideAudience().ifPresent(audience -> audience.sendMessage(data.message()));
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    /**
     * Data for a {@link SendMessageEffect}.
     *
     * @param audienceProviderKey A {@link Key} to the {@link SendMessageEffect}'s {@link AudienceProvider}
     * @param message             The {@link Component} to send to the {@link Audience}
     */
    public record Data(@NotNull Key audienceProviderKey, @NotNull Component message) implements Keyed {

        /**
         * The serial {@link Key} for this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.send_message");

        /**
         * Creates a {@link Data}.
         *
         * @param audienceProviderKey A {@link Key} to the {@link SendMessageEffect}'s {@link AudienceProvider}
         * @param message             The {@link Component} to send to the {@link Audience}
         */
        public Data {
            Objects.requireNonNull(audienceProviderKey, "audienceProviderKey");
            Objects.requireNonNull(message, "message");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }

    }

}
