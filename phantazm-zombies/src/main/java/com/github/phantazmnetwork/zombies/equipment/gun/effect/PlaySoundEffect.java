package com.github.phantazmnetwork.zombies.equipment.gun.effect;

import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class PlaySoundEffect implements Consumer<Gun> {

    private final Sound sound;

    public PlaySoundEffect(@NotNull Sound sound) {
        this.sound = sound;
    }

    public void accept(@NotNull Gun gun) {
        gun.getOwner().getPlayer().ifPresent(player -> player.playSound(sound, Sound.Emitter.self()));
    }

}
