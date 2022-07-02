package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

public class PlaySoundEffect implements GunEffect {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.effect.play_sound");

    private final Sound sound;

    public PlaySoundEffect(@NotNull Sound sound) {
        this.sound = sound;
    }

    public void accept(@NotNull Gun gun) {
        gun.getOwner().getPlayer().ifPresent(player -> player.playSound(sound, Sound.Emitter.self()));
    }

    @Override
    public void tick(long time) {

    }

    @Override
    public @NotNull Key getSerialKey() {
        return SERIAL_KEY;
    }
}
