package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class SoundShotHandler implements ShotHandler {

    public record Data(@NotNull Sound sound, @NotNull Sound headshotSound) implements VariantSerializable {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.hit_handler.sound");

        public Data {
            Objects.requireNonNull(sound, "sound");
            Objects.requireNonNull(headshotSound, "headshotSound");
        }

        @Override
        public @NotNull Key getSerialKey() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    public SoundShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public void handle(@NotNull GunState state, @NotNull Player attacker, @NotNull Collection<PhantazmMob> previousHits, @NotNull GunShot shot) {
        if (!shot.regularTargets().isEmpty()) {
            attacker.playSound(data.sound(), Sound.Emitter.self());
        }
        else if (!shot.headshotTargets().isEmpty()) {
            attacker.playSound(data.headshotSound(), Sound.Emitter.self());
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    @Override
    public @NotNull VariantSerializable getData() {
        return data;
    }
}
