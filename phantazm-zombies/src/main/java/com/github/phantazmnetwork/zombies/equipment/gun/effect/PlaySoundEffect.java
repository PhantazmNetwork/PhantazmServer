package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlaySoundEffect implements GunEffect {

    public record Data(@NotNull Sound sound) implements VariantSerializable {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.play_sound");

        public Data {
            Objects.requireNonNull(sound, "sound");
        }

        @Override
        public @NotNull Key getSerialKey() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    private final PlayerView playerView;

    public PlaySoundEffect(@NotNull Data data, @NotNull PlayerView playerView) {
        this.data = Objects.requireNonNull(data, "data");
        this.playerView = Objects.requireNonNull(playerView, "playerView");
    }

    @Override
    public void accept(@NotNull GunState state) {
        playerView.getPlayer().ifPresent(player -> player.playSound(data.sound(), Sound.Emitter.self()));
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @Override
    public @NotNull VariantSerializable getData() {
        return data;
    }

}
