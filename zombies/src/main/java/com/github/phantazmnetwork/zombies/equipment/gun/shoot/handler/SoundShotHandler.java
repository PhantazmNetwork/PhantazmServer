package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.audience.AudienceProvider;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * A {@link ShotHandler} that plays a {@link Sound}.
 */
public class SoundShotHandler implements ShotHandler {

    private final Data data;
    private final AudienceProvider audienceProvider;

    /**
     * Creates a {@link SoundShotHandler}.
     *
     * @param data             The {@link SoundShotHandler}'s {@link Data}
     * @param audienceProvider The {@link SoundShotHandler}'s {@link AudienceProvider}
     */
    public SoundShotHandler(@NotNull Data data, @NotNull AudienceProvider audienceProvider) {
        this.data = Objects.requireNonNull(data, "data");
        this.audienceProvider = Objects.requireNonNull(audienceProvider, "audienceProvider");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Key> keyProcessor = ConfigProcessors.key();
        ConfigProcessor<Sound> soundProcessor = ConfigProcessors.sound();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key audienceProviderKey =
                        keyProcessor.dataFromElement(element.getElementOrThrow("audienceProviderKey"));
                Sound sound = soundProcessor.dataFromElement(element.getElementOrThrow("sound"));
                Sound headshotSound = soundProcessor.dataFromElement(element.getElementOrThrow("headshotSound"));

                return new Data(audienceProviderKey, sound, headshotSound);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(3);
                node.put("audienceProviderKey", keyProcessor.elementFromData(data.audienceProviderKey));
                node.put("sound", soundProcessor.elementFromData(data.sound()));
                node.put("headshotSound", soundProcessor.elementFromData(data.headshotSound()));

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
    public void handle(@NotNull GunState state, @NotNull Entity attacker, @NotNull Collection<UUID> previousHits,
            @NotNull GunShot shot) {
        audienceProvider.provideAudience().ifPresent(audience -> {
            Set<UUID> played = Collections.newSetFromMap(new IdentityHashMap<>(shot.regularTargets().size()));
            for (GunHit hit : shot.regularTargets()) {
                if (played.add(hit.entity().getUuid())) {
                    audience.playSound(data.sound(), hit.entity());
                }
            }

            played.clear();
            for (GunHit hit : shot.headshotTargets()) {
                if (played.add(hit.entity().getUuid())) {
                    audience.playSound(data.headshotSound(), hit.entity());
                }
            }
        });
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    /**
     * Data for a {@link SoundShotHandler}.
     *
     * @param audienceProviderKey A {@link Key} to the {@link SoundShotHandler}'s {@link AudienceProvider}
     * @param sound               The sound to play for regular targets
     * @param headshotSound       The sound to play for headshots
     */
    public record Data(@NotNull Key audienceProviderKey, @NotNull Sound sound, @NotNull Sound headshotSound)
            implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.shot_handler.sound");

        /**
         * Creates a {@link Data}.
         *
         * @param audienceProviderKey A {@link Key} to the {@link SoundShotHandler}'s {@link AudienceProvider}
         * @param sound               The sound to play for regular targets
         * @param headshotSound       The sound to play for headshots
         */
        public Data {
            Objects.requireNonNull(audienceProviderKey, "audienceProviderKey");
            Objects.requireNonNull(sound, "sound");
            Objects.requireNonNull(headshotSound, "headshotSound");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

}
