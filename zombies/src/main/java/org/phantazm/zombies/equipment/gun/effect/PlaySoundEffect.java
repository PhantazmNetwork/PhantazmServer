package org.phantazm.zombies.equipment.gun.effect;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.audience.AudienceProvider;

import java.util.Objects;

/**
 * A {@link GunEffect} that plays a {@link Sound}.
 */
@Model("zombies.gun.effect.play_sound")
public class PlaySoundEffect implements GunEffect {

    private final Data data;
    private final AudienceProvider audienceProvider;

    /**
     * Creates a {@link PlaySoundEffect}.
     *
     * @param data             The {@link Data} for this {@link PlaySoundEffect}
     * @param audienceProvider The {@link AudienceProvider} for this {@link PlaySoundEffect}
     */
    @FactoryMethod
    public PlaySoundEffect(@NotNull Data data,
            @NotNull @DataName("audience_provider") AudienceProvider audienceProvider) {
        this.data = Objects.requireNonNull(data, "data");
        this.audienceProvider = Objects.requireNonNull(audienceProvider, "audienceProvider");
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
     * @param audienceProviderPath A path to the {@link PlaySoundEffect}'s {@link AudienceProvider}
     * @param sound                The {@link Sound} to play
     */
    @DataObject
    public record Data(@NotNull @DataPath("audience_provider") String audienceProviderPath, @NotNull Sound sound) {

        /**
         * Creates a {@link Data}.
         *
         * @param audienceProviderPath A path to the {@link PlaySoundEffect}'s {@link AudienceProvider}
         * @param sound                The {@link Sound} to play
         */
        public Data {
            Objects.requireNonNull(audienceProviderPath, "audienceProviderPath");
            Objects.requireNonNull(sound, "sound");
        }

    }

}
