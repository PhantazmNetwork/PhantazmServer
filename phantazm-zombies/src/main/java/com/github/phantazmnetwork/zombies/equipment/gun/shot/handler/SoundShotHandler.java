package com.github.phantazmnetwork.zombies.equipment.gun.shot.handler;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.zombies.equipment.gun.Gun;
import com.github.phantazmnetwork.zombies.equipment.gun.shot.GunShot;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SoundShotHandler implements ShotHandler {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.hit_handler.sound");

    private final Sound sound;

    private final Sound headshotSound;

    public SoundShotHandler(@NotNull Sound sound, @NotNull Sound headshotSound) {
        this.sound = Objects.requireNonNull(sound, "sound");
        this.headshotSound = Objects.requireNonNull(headshotSound, "headshotSound");
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull Player attacker, @NotNull GunShot shot) {
        if (!shot.getRegularTargets().isEmpty()) {
            attacker.playSound(sound, Sound.Emitter.self());
        }
        else if (!shot.getHeadshotTargets().isEmpty()) {
            attacker.playSound(headshotSound, Sound.Emitter.self());
        }
    }

    @Override
    public void tick(long time) {

    }

    @Override
    public @NotNull Key getSerialKey() {
        return SERIAL_KEY;
    }
}
